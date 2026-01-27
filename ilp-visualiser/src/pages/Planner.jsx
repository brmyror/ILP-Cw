import { useState } from "react";
import DispatchForm from "../components/DispatchForm.jsx";
import DispatchTable from "../components/DispatchTable.jsx";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import dispatchScenarioData from "../data/dispatch-scenario.json";

export default function Planner() {
    const [dispatches, setDispatches] = useState([]);
    const navigate = useNavigate();
    const [loading, setLoading] = useState(false);

    // arrays of dispatch objects that match the shape produced by DispatchForm.
    // Each use-case has an id, a human-friendly name, and a dispatches array.
    const [useCases] = useState(dispatchScenarioData.useCases ?? []);

    const [selectedUseCaseId, setSelectedUseCaseId] = useState(null);

    function addDispatch(dispatchData) {
        // use functional updater to avoid stale closures
        setDispatches(prev => [...prev, dispatchData]);
        // Clear any explicit use-case selection once the user starts manual editing
        setSelectedUseCaseId(null);
    }

    function handleSelectUseCase(useCase) {
        // Replace current dispatch list with the selected use case's dispatches
        setDispatches(useCase.dispatches || []);
        setSelectedUseCaseId(useCase.id);
    }

    async function handleSubmit() {
        try {
            console.log("Sending:", dispatches);
            setLoading(true);
            const response = await axios.post(
                "http://localhost:8080/api/v1/calcDeliveryPath",
                dispatches
            );
            setLoading(false);
            navigate("/visualiser", { state: { result: response.data } });
        } catch (err) {
            setLoading(false);

            if (err.response) {
                // backend returned JSON error
                alert("Backend error: " + JSON.stringify(err.response.data));
            } else {
                alert("Cannot connect to CW2 service");
            }

            console.error(err);
        }
    }

    return (
        <div style={{ padding: "20px" }}>
            <h2>Med Dispatch Planner</h2>

            {/* Top: form + map (inside DispatchForm) */}
            <DispatchForm onAdd={addDispatch} />

            {/* Bottom row: left = dispatch table, right = use case instances */}
            <div style={{ display: "flex", alignItems: "flex-start", gap: "24px", marginTop: "-50px" }}>
                <div style={{ flex: 2 }}>
                    <DispatchTable
                        dispatches={dispatches}
                        onDelete={(id) => setDispatches(prev => prev.filter(d => d.id !== id))}
                    />

                    <button onClick={handleSubmit} disabled={loading}>
                        {loading ? "Calculating..." : "Calculate Delivery Path"}
                    </button>
                </div>

                {/* Visibly separated Use Case Instances panel */}
                <aside
                    style={{
                        flex: 1,
                        border: "1px solid #ccc",
                        borderRadius: 8,
                        padding: 12,
                        background: "#fafafa",
                        minWidth: 260,
                    }}
                >
                    <h3 style={{ marginTop: 0 }}>Sample Use Case Scenarios</h3>
                    <p style={{ fontSize: 13, color: "#555" }}>
                        Select a predefined scenario to load its dispatch list into the table.
                    </p>

                    <div style={{ display: "flex", flexDirection: "column", gap: 8, marginTop: 8 }}>
                        {useCases.map(uc => (
                            <button
                                key={uc.id}
                                type="button"
                                onClick={() => handleSelectUseCase(uc)}
                                style={{
                                    textAlign: "left",
                                    padding: "8px 10px",
                                    borderRadius: 6,
                                    border: selectedUseCaseId === uc.id ? "2px solid #0077cc" : "1px solid #ccc",
                                    background: selectedUseCaseId === uc.id ? "#e6f2ff" : "#fff",
                                    cursor: "pointer",
                                }}
                            >
                                <div style={{ fontWeight: 600, fontSize: 14 }}>{uc.name}</div>
                                {uc.description && (
                                    <div style={{ fontSize: 12, color: "#666" }}>{uc.description}</div>
                                )}
                                {Array.isArray(uc.dispatches) && uc.dispatches.length > 0 && (
                                    <div style={{ fontSize: 11, color: "#999", marginTop: 2 }}>
                                        {uc.dispatches.length} dispatch(es)
                                    </div>
                                )}
                            </button>
                        ))}
                    </div>
                </aside>
            </div>
        </div>
    );
}
