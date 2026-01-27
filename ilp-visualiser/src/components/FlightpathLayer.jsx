import { Polyline, Marker, Popup, useMap, CircleMarker, Tooltip } from "react-leaflet";
import L from "leaflet";
import { useEffect, useMemo } from "react";

export default function FlightpathLayer({ data, step = Infinity, playing = false }) {

    const drones = useMemo(() => Array.isArray(data) ? data : [data], [data]);
    const map = useMap();

    // Colours for each drone
    const colours = ["red", "blue", "green", "purple", "orange", "black"];

    useEffect(() => {
        const bounds = [];

        drones.forEach(d => {
            d.flightPath.forEach(p => bounds.push([p.lat, p.lng]));
            (d.deliveryPoints || []).forEach(p => bounds.push([p.lat, p.lng]));
        });

        if (bounds.length > 0) {
            map.fitBounds(bounds, { padding: [50, 50] });
        }
    }, [drones, map]);

    // Drone base (from ILP spec)
    const AT = [55.9446806670849, -3.18635807889864];
    const OT = [55.9811862793337, -3.17732611501824];

    // create a simple div icon for bases
    function createBaseIcon(label, color) {
        return L.divIcon({
            className: '',
            html: `<div style="background:${color};color:#fff;padding:4px 6px;border-radius:4px;font-weight:700;font-size:12px;border:2px solid #fff;box-shadow:0 1px 3px rgba(0,0,0,0.3)">${label}</div>`,
            iconSize: [34, 20],
            iconAnchor: [17, 10],
        });
    }

    return (
        <>
            {/* Base markers with distinct icons */}
            <Marker position={AT} icon={createBaseIcon('AT', '#2b6cb0')}>
                <Popup>Drone Base - Appleton Tower</Popup>
            </Marker>
            <Marker position={OT} icon={createBaseIcon('OT', '#2f855a')}>
                <Popup>Drone Base - Ocean Terminal</Popup>
            </Marker>

            {drones.map((drone, index) => {
                const colour = colours[index % colours.length];

                const pathLatLngs = (drone.flightPath || []).map(p => [p.lat, p.lng]);

                // build a map of delivery points for quick lookup
                const deliveryMap = new Map();
                (drone.deliveryPoints || []).forEach(dp => {
                    const key = `${Number(dp.lat).toFixed(6)},${Number(dp.lng).toFixed(6)}`;
                    deliveryMap.set(key, dp.deliveryId);
                });

                // compute which portion to display based on `step`
                const displayCount = Math.max(0, Math.min(pathLatLngs.length, Number.isFinite(step) ? step : pathLatLngs.length));
                const displayed = pathLatLngs.slice(0, displayCount);

                return (
                    <div key={index}>
                        {/* Flightpath polyline (only drawn for displayed segment) */}
                        <Polyline
                            positions={displayed}
                            pathOptions={{ color: colour, weight: 4 }}
                        />

                        {/* Per-move markers: when playing, show only the active (last) marker to reduce clutter; otherwise show all markers */}
                        {displayed.map((pt, i) => {
                            const lat = pt[0];
                            const lng = pt[1];

                            const originalIndex = i; // step index within this drone
                            const isActive = (displayCount > 0 && originalIndex === displayCount - 1);

                            // if playing, only render the active marker
                            if (playing && !isActive) return null;

                            // simple move marker, highlight if active
                            return (
                                <CircleMarker key={`pt-${index}-${i}`} center={[lat, lng]} radius={isActive ? 6 : 4} pathOptions={{ color: colour, fillColor: colour, fillOpacity: 0.8 }}>
                                    <Tooltip direction="top">Step {originalIndex}</Tooltip>
                                </CircleMarker>
                            );
                        })}

                        {/* Delivery point markers (final confirmations) - these are shown when reached */}
                        {drone.deliveryPoints.map((p, i) => {
                            const key = `${Number(p.lat).toFixed(6)},${Number(p.lng).toFixed(6)}`;
                            // find when this delivery would appear in the path
                            const deliveryIndex = pathLatLngs.findIndex(pp => `${Number(pp[0]).toFixed(6)},${Number(pp[1]).toFixed(6)}` === key);
                            if (deliveryIndex === -1) return null;
                            if (deliveryIndex >= displayCount) return null; // not reached yet
                            return (
                                <Marker key={`del-${index}-${i}`} position={[p.lat, p.lng]}>
                                    <Popup>
                                        <b>Delivery #{p.deliveryId}</b><br />
                                        Drone {drone.droneId}<br />
                                        Lat: {p.lat.toFixed(6)}<br />
                                        Lng: {p.lng.toFixed(6)}
                                    </Popup>
                                </Marker>
                            );
                        })}
                    </div>
                );
            })}
        </>
    );
}
