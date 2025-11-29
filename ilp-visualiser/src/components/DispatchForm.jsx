import { useState, useRef } from "react";
import { MapContainer, TileLayer, Marker, useMapEvents, GeoJSON } from "react-leaflet";
import restrictedAreasRaw from "../data/restricted-areas.json";

export default function DispatchForm({ onAdd }) {
    const [id, setId] = useState(1);
    const [date, setDate] = useState("");
    const [time, setTime] = useState("");
    const [capacity, setCapacity] = useState("");
    const [temperatureReq, setTemperatureReq] = useState("none");
    const [maxCost, setMaxCost] = useState("");
    const [lng, setLng] = useState("");
    const [lat, setLat] = useState("");

    // Map UI state
    const [mapInstance, setMapInstance] = useState(null);

    // Keep a ref of rendered GeoJSON polygon layers so we can open/popups and highlight them
    const restrictedLayersRef = useRef({});
    const [coordError, setCoordError] = useState("");
    const [coordErrorField, setCoordErrorField] = useState("");
    const [capacityError, setCapacityError] = useState("");
    const [maxCostError, setMaxCostError] = useState("");

    function handleSubmit(e) {
        e.preventDefault();

        // Final validation before creating dispatch - collect errors; show inline messages and focus first invalid field
        const errors = [];
        const capNum = Number(capacity);
        const latNum = parseFloat(lat);
        const lngNum = parseFloat(lng);
        const maxCostNum = Number(maxCost);
        let firstInvalidField = null;

        if (!Number.isFinite(capNum) || capNum <= 0) {
            errors.push('Capacity must be a number greater than 0');
            setCapacityError('Capacity must be a number greater than 0');
            firstInvalidField ??= 'capacity';
        } else {
            setCapacityError('');
        }

        if (String(lng).trim() === '' || String(lat).trim() === '') {
            errors.push('Please enter a valid longitude and latitude');
            setCoordError('Please enter a valid longitude and latitude');
            setCoordErrorField('lng');
            firstInvalidField ??= 'lng';
        } else {
            if (!Number.isFinite(lngNum) || lngNum < -180 || lngNum > 180) {
                errors.push('Longitude must be between -180 and 180');
                setCoordError('Longitude must be between -180 and 180');
                setCoordErrorField('lng');
                firstInvalidField ??= 'lng';
            }
            if (!Number.isFinite(latNum) || latNum < -90 || latNum > 90) {
                errors.push('Latitude must be between -90 and 90');
                setCoordError('Latitude must be between -90 and 90');
                setCoordErrorField('lat');
                firstInvalidField ??= 'lat';
            }
        }

        // Check restricted area
        if (Number.isFinite(latNum) && Number.isFinite(lngNum)) {
            const foundOnSubmit = findContainingFeature({ lat: latNum, lng: lngNum });
            if (foundOnSubmit) {
                errors.push(`Selected coordinates are inside restricted area: ${foundOnSubmit.properties.name}`);
                // open/highlight the polygon
                const layer = restrictedLayersRef.current[foundOnSubmit.properties.id];
                if (layer) {
                    try { layer.setStyle({ fillOpacity: 0.5 }); } catch (err) { console.warn(err); }
                    setTimeout(() => {
                        try { layer.setStyle({ fillOpacity: 0.25 }); } catch (err) { console.warn(err); }
                    }, 1200);
                }
                firstInvalidField ??= 'lng';
            }
        }

        if (maxCost !== '' && (!Number.isFinite(maxCostNum) || maxCostNum < 0)) {
            errors.push('Max cost must be a non-negative number');
            setMaxCostError('Max cost must be a non-negative number');
        } else {
            setMaxCostError('');
        }

        if (errors.length > 0) {
            // Focus first invalid field for clarity; field-level inline errors are already set
            if (firstInvalidField) {
                const el = document.getElementById(firstInvalidField);
                if (el && typeof el.focus === 'function') el.focus();
            }
            // rely on inline errors only
            return;
        }

        const idNum = parseInt(id, 10);
        const dispatch = {
            id: Number.isFinite(idNum) ? idNum : 1,
            date,
            time,
            requirements: {
                capacity: capNum,
                cooling: temperatureReq === 'cooling',
                heating: temperatureReq === 'heating',
                maxCost: maxCost === '' ? undefined : maxCostNum,
            },
            delivery: {
                lng: lngNum,
                lat: latNum
            }
        };

        onAdd(dispatch);

        // Clear form and increment id numerically (guard against string concatenation)
        // increment id numerically (guard if id is string/number)
        setId(prev => {
            const n = Number(prev);
            return Number.isFinite(n) ? n + 1 : 1;
        });
        setDate("");
        setTime("");
        setCapacity("");
        setTemperatureReq("none");
        setMaxCost("");
        setLng("")
        setLat("")
        setCoordError("");
        setCoordErrorField("");
        setCapacityError("");
        setMaxCostError("");
    }

    // Validate manual coordinates typed into inputs. If inside restricted area, set error and highlight.
    function validateManualCoords(latVal, lngVal) {
        const latNum = Number(latVal);
        const lngNum = Number(lngVal);

        // If either is not a valid number yet, clear any error and bail out
        if (!isFinite(latNum) || !isFinite(lngNum)) {
            setCoordError("");
            setCoordErrorField("");
            return false;
        }

        // Range checks
        if (lngNum < -180 || lngNum > 180) {
            setCoordError("Longitude must be between -180 and 180");
            setCoordErrorField('lng');
            return true;
        }
        if (latNum < -90 || latNum > 90) {
            setCoordError("Latitude must be between -90 and 90");
            setCoordErrorField('lat');
            return true;
        }

        const found = findContainingFeature({ lat: latNum, lng: lngNum });
        if (found) {
            // highlight and open the polygon
            const layer = restrictedLayersRef.current[found.properties.id];
            if (layer) {
                try { layer.setStyle({ fillOpacity: 0.5 }); } catch (err) { console.warn(err); }
                setTimeout(() => {
                    try { layer.setStyle({ fillOpacity: 0.25 }); } catch (err) { console.warn(err); }
                }, 1200);
            }
            setCoordError(`Coordinates fall inside restricted area: ${found.properties.name}`);
            setCoordErrorField('lng');
            return true;
        }

        // clear any coordinate-field specific flag when validation passes
        setCoordError("");
        setCoordErrorField("");
        return false;
    }

    function handleLngChange(e) {
        const newLng = e.target.value;
        setLng(newLng);
        // validate using the current lat state (may be empty)
        validateManualCoords(lat, newLng);
    }

    function handleLatChange(e) {
        const newLat = e.target.value;
        setLat(newLat);
        validateManualCoords(newLat, lng);
    }

    function handleCapacityChange(e) {
        const v = e.target.value;
        setCapacity(v);
        const n = Number(v);
        if (!isFinite(n) || n <= 0) {
            setCapacityError('Capacity must be a number greater than 0');
        } else {
            setCapacityError('');
        }
    }

    function handleMaxCostChange(e) {
        const v = e.target.value;
        setMaxCost(v);
        if (v === '') { setMaxCostError(''); return; }
        const n = Number(v);
        if (!isFinite(n) || n < 0) {
            setMaxCostError('Max cost must be a non-negative number');
        } else {
            setMaxCostError('');
        }
    }

    // Map click handler component (reports latlng back to parent)
    function ClickSelector({ onSelect }) {
        useMapEvents({
            click(e) {
                onSelect(e.latlng);
            }
        });
        return null;
    }

    // Default map center (Edinburgh-ish) if no location selected
    const defaultCenter = [55.958046, -3.188436];

    // Convert the raw areas into a GeoJSON FeatureCollection with properties
    function convertRawToGeoJSON(rawAreas) {
        const features = rawAreas.map(area => {
            const ring = area.vertices.map(v => [v.lng, v.lat]);
            // ensure closed
            if (ring.length && (ring[0][0] !== ring[ring.length - 1][0] || ring[0][1] !== ring[ring.length - 1][1])) {
                ring.push(ring[0]);
            }
            return {
                type: 'Feature',
                properties: { name: area.name, id: area.id },
                geometry: { type: 'Polygon', coordinates: [ring] }
            };
        });
        return { type: 'FeatureCollection', features };
    }

    const restrictedGeoJSON = convertRawToGeoJSON(restrictedAreasRaw);

    // Point-in-polygon check (ray-casting). polygon: array of [lng, lat]
    function pointInPolygon(lat, lng, polygon) {
        let x = lng, y = lat;
        let inside = false;
        for (let i = 0, j = polygon.length - 1; i < polygon.length; j = i++) {
            const xi = polygon[i][0], yi = polygon[i][1];
            const xj = polygon[j][0], yj = polygon[j][1];

            const intersect = ((yi > y) !== (yj > y)) &&
                (x < (xj - xi) * (y - yi) / (yj - yi) + xi);
            if (intersect) inside = !inside;
        }
        return inside;
    }

    // Find which restricted feature (if any) contains the latlng
    function findContainingFeature(latlng) {
        if (!restrictedGeoJSON || !restrictedGeoJSON.features) return null;
        for (const feature of restrictedGeoJSON.features) {
            if (!feature.geometry || feature.geometry.type !== 'Polygon') continue;
            const ring = feature.geometry.coordinates[0]; // [ [lng,lat], ... ]
            if (pointInPolygon(latlng.lat, latlng.lng, ring)) return feature;
        }
        return null;
    }

    return (
        <div style={{ marginTop: "0px" }}>
            <h3>Add Dispatch</h3>

            {/* using inline errors and alerts */}

            <div style={{ display: "flex", gap: "1rem", alignItems: "flex-start" }}>
                {/* Left: the form */}
                <form onSubmit={handleSubmit} noValidate style={{ minWidth: 320 }}>
                    {/* ID is auto-generated and read-only. Keep a hidden input for form semantics. */}
                    <input id="id" type="hidden" value={id} />
                    <div style={{ marginBottom: 0 }}><strong>ID:</strong> <span>{id}</span></div>

                    {/* field-level inline errors shown under each input; no global error list */}

                    <label htmlFor="date">Date: </label>
                    <input id="date" type="date" value={date} onChange={e => setDate(e.target.value)} />

                    <br />

                    <label htmlFor="time">Time: </label>
                    <input id="time" type="time" value={time} onChange={e => setTime(e.target.value)} />

                    <br />

                    <label htmlFor="capacity">Capacity: </label>
                    <input id="capacity" type="number" min="1" step="1" value={capacity} onChange={handleCapacityChange} required aria-describedby="capacityError" />
                    {capacityError && <div id="capacityError" style={{ color: 'crimson', fontSize: 12 }}>{capacityError}</div>}

                    <br />

                    <fieldset style={{ border: 'none', padding: 0, margin: 0 }}>
                        <legend style={{ fontSize: 16 }}>Temperature requirement:</legend>
                        <label style={{ marginRight: 8 }}><input type="radio" name="tempReq" value="none" checked={temperatureReq === 'none'} onChange={() => setTemperatureReq('none')} /> None</label>
                        <label style={{ marginRight: 8 }}><input type="radio" name="tempReq" value="cooling" checked={temperatureReq === 'cooling'} onChange={() => setTemperatureReq('cooling')} /> Cooling</label>
                        <label><input type="radio" name="tempReq" value="heating" checked={temperatureReq === 'heating'} onChange={() => setTemperatureReq('heating')} /> Heating</label>
                    </fieldset>

                    <label htmlFor="maxCost">Max Cost: </label>
                    <input id="maxCost" type="number" min="0" step="0.01" value={maxCost} onChange={handleMaxCostChange} aria-describedby="maxCostError" />
                    {maxCostError && <div id="maxCostError" style={{ color: 'crimson', fontSize: 12 }}>{maxCostError}</div>}

                    <br />

                    <label htmlFor="lng">Longitude: </label>
                    <input id="lng" type="number" step="0.000001" min="-180" max="180" value={lng} onChange={handleLngChange} required aria-describedby="coordError" />

                    <br />

                    <label htmlFor="lat">Latitude: </label>
                    <input id="lat" type="number" step="0.000001" min="-90" max="90" value={lat} onChange={handleLatChange} required aria-describedby="coordError" />

                    {coordError && coordErrorField === 'lng' && (
                        <div id="coordError" style={{ color: 'crimson', fontSize: 12, marginTop: 6 }}>{coordError}</div>
                    )}

                    <br />

                    {coordError && coordErrorField === 'lat' && (
                        <div id="coordErrorLat" style={{ color: 'crimson', fontSize: 12, marginTop: 6 }}>{coordError}</div>
                    )}

                    <br />

                    <button type="submit">Add</button>
                </form>

                {/* Right: the map - clicking it will set lat/lng fields (unless restricted) */}
                <div style={{ width: 520, border: "1px solid #ccc", padding: 8, borderRadius: 6 }}>
                    <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", gap: 8, marginBottom: 6 }}>
                        <div>
                            <div style={{ fontSize: 12, color: "#666" }}>OpenStreetMap</div>
                        </div>

                        <div style={{ textAlign: "right" }}>
                            <button type="button" onClick={() => {
                                setLng("");
                                setLat("");
                                // Recenter map to default
                                if (mapInstance) mapInstance.setView(defaultCenter, 13);
                            }}>Clear location</button>
                        </div>
                    </div>

                    <div style={{ fontSize: 12, color: "#666", marginBottom: 6 }}>Click the map to choose delivery location — coordinates will populate the form unless the location is inside a restricted area.</div>

                    <div style={{ width: "100%", height: 300 }}>
                        <MapContainer
                            center={lat && lng ? [Number(lat), Number(lng)] : defaultCenter}
                            zoom={12}
                            style={{ width: "100%", height: "100%" }}
                            scrollWheelZoom={true}
                            whenCreated={map => setMapInstance(map)}
                        >
                            {/* Always use OSM tiles */}
                            <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" attribution='&copy; <a href="https://www.openstreetmap.org/">OpenStreetMap</a> contributors' />

                            {/* Render restricted areas (if any) */}
                            {restrictedGeoJSON.features && restrictedGeoJSON.features.length > 0 && (
                                <GeoJSON
                                    data={restrictedGeoJSON}
                                    style={() => ({ color: 'red', weight: 2, fillOpacity: 0.25 })}
                                    onEachFeature={(feature, layer) => {
                                        // store layer so we can open/flash it when clicked on
                                        if (feature.properties && feature.properties.id != null) {
                                            restrictedLayersRef.current[feature.properties.id] = layer;
                                        }

                                        if (feature.properties && feature.properties.name) {
                                            layer.bindPopup(feature.properties.name);
                                        } else {
                                            layer.bindPopup('Restricted area');
                                        }
                                    }}
                                />
                            )}

                            <ClickSelector onSelect={(latlng) => {
                                // Prevent selection inside any restricted polygon
                                const found = findContainingFeature(latlng);
                                if (found) {
                                    // open and briefly highlight the polygon
                                    const layer = restrictedLayersRef.current[found.properties.id];
                                    if (layer) {
                                        try { layer.setStyle({ fillOpacity: 0.5 }); } catch (err) { console.warn(err); }
                                        setTimeout(() => {
                                            try { layer.setStyle({ fillOpacity: 0.25 }); } catch (err) { console.warn(err); }
                                        }, 1200);
                                    }

                                    // set inline coordinate error and focus the longitude field
                                    setCoordError(`That location is inside a restricted area: ${found.properties.name}`);
                                    setCoordErrorField('lng');
                                    const lngEl = document.getElementById('lng');
                                    if (lngEl && typeof lngEl.focus === 'function') lngEl.focus();
                                    return;
                                 }

                                const formattedLat = latlng.lat.toFixed(6);
                                const formattedLng = latlng.lng.toFixed(6);
                                setLat(String(formattedLat));
                                setLng(String(formattedLng));
                                // center map on selection
                                if (mapInstance) mapInstance.setView([latlng.lat, latlng.lng], mapInstance.getZoom());
                            }} />

                            {/* Show marker if a lat/lng has been selected */}
                            {lat && lng && (
                                <Marker position={[Number(lat), Number(lng)]} />
                            )}
                        </MapContainer>
                    </div>
                </div>
            </div>
        </div>
    );
}
