/** Labelled input with inline error message. */
export default function FormField({ label, error, children }) {
  return (
    <div className={`form-field ${error ? "has-error" : ""}`}>
      <label>{label}</label>
      {children}
      {error && <div className="field-error">{error}</div>}
    </div>
  );
}
