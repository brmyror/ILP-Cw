import styles from './DispatchTable.module.css';

export default function DispatchTable({ dispatches = [], onDelete = id => {} }) {

    {/* Helper function to format coordinates for display */}
    function formatVal(val, precision) {
        if (val === undefined || val === null || val === '') return '';
        const n = Number(val);
        return Number.isFinite(n) ? n.toFixed(precision) : String(val);
    }

    return (
        <div className={styles.container}>
            <h3>Dispatches Added:</h3>

            {/* If no dispatches, show a message; otherwise render the table */}
            {dispatches.length === 0 ? (
                <div className={styles.empty}>No dispatches added.</div>
            ) : (
                <div className={styles.tableWrap}>
                    <table className={styles.table}>
                        <thead>
                            <tr>
                                {/* Table headers are defined in the CSS */}
                                <th className={styles.th}>ID</th>
                                <th className={styles.th}>Date</th>
                                <th className={styles.th}>Time</th>
                                <th className={`${styles.th} ${styles.thRight}`}>Capacity</th>
                                <th className={`${styles.th} ${styles.thCenter}`}>Cooling</th>
                                <th className={`${styles.th} ${styles.thCenter}`}>Heating</th>
                                <th className={`${styles.th} ${styles.thRight}`}>Max Cost</th>
                                <th className={`${styles.th} ${styles.thRight}`}>Longitude</th>
                                <th className={`${styles.th} ${styles.thRight}`}>Latitude</th>
                                <th className={`${styles.th} ${styles.thCenter}`}>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {dispatches.map((d, index) => (
                                <tr key={d?.id ?? index}>
                                    <td className={styles.td}>{d?.id}</td>
                                    <td className={styles.td}>{d?.date || ''}</td>
                                    <td className={styles.td}>{d?.time || ''}</td>
                                    <td className={`${styles.td} ${styles.tdRight}`}>{d?.requirements?.capacity ?? ''}</td>
                                    <td className={`${styles.td} ${styles.tdCenter}`}>{d?.requirements?.cooling ? 'Yes' : 'No'}</td>
                                    <td className={`${styles.td} ${styles.tdCenter}`}>{d?.requirements?.heating ? 'Yes' : 'No'}</td>
                                    <td className={`${styles.td} ${styles.tdRight}`}>{formatVal(d?.requirements?.maxCost, 2)}</td>
                                    <td className={`${styles.td} ${styles.tdRight}`}>{formatVal(d?.delivery?.lng, 6)}</td>
                                    <td className={`${styles.td} ${styles.tdRight}`}>{formatVal(d?.delivery?.lat, 6)}</td>
                                    <td className={styles.actionsCell}>
                                        <button
                                            type="button"
                                            onClick={() => onDelete(d?.id)}
                                            className={styles.deleteBtn}
                                        >
                                            Delete
                                        </button>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    );
}
