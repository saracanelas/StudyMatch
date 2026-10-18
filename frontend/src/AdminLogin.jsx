import { useState } from "react";
import { loginAdmin } from "./Admin.js";
import "./AdminLogin.css";

export default function Login({ onLogin }) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  // Show authentication errors and prevent duplicate submissions.
  const [error, setError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setIsSubmitting(true);

    try {
      // loginAdmin calls the backend authentication endpoint.
      await loginAdmin(email, password);

      onLogin();
    } catch (err) {
      // Keep the form visible and show the backend error to the user.
      setError(err.message);
    } finally {
      // Re-enable the button after the request finishes.
      setIsSubmitting(false);
    }
  };
  return (
    <div className="login-page">
      {/* Background forms */}
      <svg className="blob blob-top" viewBox="0 0 400 260" preserveAspectRatio="none" aria-hidden="true">
        <defs>
          <linearGradient id="gTop" x1="0" y1="1" x2="1" y2="0">
            <stop offset="0%" stopColor="#DBB8E3" />
            <stop offset="100%" stopColor="#E9D7FA" />
          </linearGradient>
        </defs>
        <path
          d="M0 0 H400 V230 C340 250 270 235 240 190 C215 150 225 110 180 80 C140 55 60 50 0 0 Z"
          fill="url(#gTop)"
        />
      </svg>

      <svg className="blob blob-bottom" viewBox="0 0 300 300" preserveAspectRatio="none" aria-hidden="true">
        <defs>
          <linearGradient id="gBottom" x1="0" y1="1" x2="1" y2="0">
            <stop offset="0%" stopColor="#DBB8E3" />
            <stop offset="100%" stopColor="#E9D7FA" />
          </linearGradient>
        </defs>
        <path
          d="M0 40 C60 30 105 50 112 110 C118 170 150 200 190 240 C215 265 240 285 260 300 H0 Z"
          fill="url(#gBottom)"
        />
      </svg>
        {/*login square*/}
      <form className="card" onSubmit={handleSubmit}>
        <div className="logo">UPT</div>

        <h1>Welcome back!</h1>
        <p className="subtitle">Admin Login</p>

        <label className="field">
          <span>Email</span>
          <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            autoComplete="email"
            required
          />
        </label>

        <label className="field">
          <span>Password</span>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            autoComplete="current-password"
            required
          />
        </label>

        {error && <p role="alert">{error}</p>}
        <button type="submit" className="btn-login" disabled={isSubmitting}>
          {isSubmitting ? "A entrar..." : "Login"}
        </button>
        
      </form>
    </div>
  );
}