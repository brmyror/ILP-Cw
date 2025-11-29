import { useLocation } from "react-router-dom";
import { MapContainer, TileLayer, GeoJSON } from "react-leaflet";
import FlightpathLayer from "../components/FlightpathLayer.jsx";
import restrictedAreasRaw from '../data/restricted-areas.json';
import { useState, useEffect, useRef, useMemo } from 'react';

export default function Visualiser() {
    const { state } = useLocation();
    const result = state?.result;

    // playback state (declare hooks before any conditional logic)
    const [playing, setPlaying] = useState(false);
    const [step, setStep] = useState(0); // number of points shown (0..maxSteps)
    const [speedMs, setSpeedMs] = useState(700); // ms between steps
    const [stepDelta, setStepDelta] = useState(1); // how many steps to advance per tick (1,5,10)
    const timerRef = useRef(null);

    // Normalize backend structure (memoized)
    const normalized = useMemo(() => {
        if (!result) return [];
        return result.dronePaths.map(drone => {
            const allPathPoints = drone.deliveries.flatMap(d => d.flightPath);

            const deliveryPoints = drone.deliveries
                .filter(d => d.deliveryId !== null)
                .map(d => {
                    const lastPoint = d.flightPath[d.flightPath.length - 1];
                    return {
                        deliveryId: d.deliveryId,
                        lat: lastPoint.lat,
                        lng: lastPoint.lng
                    };
                });

            return {
                droneId: drone.droneId,
                flightPath: allPathPoints,
                deliveryPoints
            };
        });
    }, [result]);

    // compute total steps (max flightPath length)
    const maxSteps = useMemo(() => normalized.reduce((max, d) => Math.max(max, (d.flightPath || []).length), 0), [normalized]);

    // advance when playing
    useEffect(() => {
        if (!playing) return undefined;
        if (step >= maxSteps) {
            // defer stopping playback to avoid synchronous state update inside effect
            setTimeout(() => setPlaying(false), 0);
            return undefined;
        }

        timerRef.current = setTimeout(() => {
            setStep(s => Math.min(maxSteps, s + stepDelta));
        }, speedMs);

        return () => clearTimeout(timerRef.current);
    }, [playing, step, speedMs, maxSteps, stepDelta]);

    // stop timer on unmount
    useEffect(() => () => clearTimeout(timerRef.current), []);

    function handlePlayPause() {
        if (playing) {
            setPlaying(false);
        } else {
            if (step >= maxSteps) setStep(0);
            setPlaying(true);
        }
    }

    function handlePrev() { setPlaying(false); setStep(s => Math.max(0, s - stepDelta)); }
    function handleNext() { setPlaying(false); setStep(s => Math.min(maxSteps, s + stepDelta)); }
    function handleReset() { setPlaying(false); setStep(0); }
    function handleFastForward() { setPlaying(false); setStep(maxSteps); }

    if (!result || result.totalCost === 0.0) {
        return <p>No result loaded.</p>;
    }

    return (
        <div style={{ padding: "20px" }}>
            <h2>Flightpath Visualiser</h2>

            <h3>Route Summary</h3>
            <p><strong>Total Cost:</strong> {result.totalCost}</p>
            <p><strong>Total Moves:</strong> {result.totalMoves}</p>

            {/* Legend */}
            <div style={{ display: 'flex', gap: 12, alignItems: 'center', marginBottom: 10 }}>
                {normalized.map((d, i) => (
                    <div key={d.droneId} style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                        <div style={{ width: 14, height: 14, background: ["red","blue","green","purple","orange","black"][i % 6], borderRadius: 2 }} />
                        <div style={{ fontSize: 13 }}>Drone #{d.droneId} path</div>
                    </div>
                ))}
            </div>

            {/* Playback controls */}
            <div style={{ marginBottom: 12, display: 'flex', gap: 8, alignItems: 'center' }}>
                <button onClick={handlePlayPause}>{playing ? 'Pause' : 'Play'}</button>
                <button onClick={handlePrev}>Prev</button>
                <button onClick={handleNext}>Next</button>
                <button onClick={handleReset}>Reset</button>
                <button onClick={handleFastForward}>End</button>

                <div style={{ display: 'flex', alignItems: 'center', gap: 6, marginLeft: 12 }}>
                    <label style={{ fontSize: 13 }}>Step:</label>
                    <input type="range" min={0} max={maxSteps} value={step} onChange={e => { setStep(Number(e.target.value)); setPlaying(false); }} />
                    <div style={{ minWidth: 48, textAlign: 'center' }}>{step} / {maxSteps}</div>
                </div>

                <div style={{ marginLeft: 12, display: 'flex', alignItems: 'center', gap: 8 }}>
                    <label style={{ fontSize: 13 }}>Speed:</label>
                    <select value={speedMs} onChange={e => setSpeedMs(Number(e.target.value))} style={{ marginLeft: 6 }}>
                        <option value={1200}>Slow</option>
                        <option value={700}>Normal</option>
                        <option value={350}>Fast</option>
                        <option value={150}>Very fast</option>
                    </select>

                    <label style={{ fontSize: 13, marginLeft: 8 }}>Advance:</label>
                    <select value={stepDelta} onChange={e => setStepDelta(Number(e.target.value))} style={{ marginLeft: 6 }}>
                        <option value={1}>1 step</option>
                        <option value={5}>5 steps</option>
                        <option value={10}>10 steps</option>
                        <option value={100}>100 steps</option>
                        <option value={1000}>1000 steps</option>
                    </select>
                </div>
             </div>

            <MapContainer
                style={{ height: "600px", width: "100%", marginTop: "20px" }}
                center={[55.944, -3.186]}
                zoom={13}
            >
                <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
                <FlightpathLayer data={normalized} step={step} playing={playing} />

                {/* Render restricted areas as GeoJSON polygons */}
                {restrictedAreasRaw && restrictedAreasRaw.length > 0 && (
                    <GeoJSON
                        data={{ type: 'FeatureCollection', features: restrictedAreasRaw.map(a => ({
                            type: 'Feature',
                            properties: { name: a.name, id: a.id },
                            geometry: { type: 'Polygon', coordinates: [a.vertices.map(v => [v.lng, v.lat])] }
                        })) }}
                        style={() => ({ color: 'red', weight: 2, fillOpacity: 0.18 })}
                        onEachFeature={(feature, layer) => {
                            if (feature.properties && feature.properties.name) layer.bindPopup(feature.properties.name);
                        }}
                    />
                )}
             </MapContainer>
        </div>
    );
}
