import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'

const s = {
  input: {
    width: '100%', padding: '0.65rem 0.85rem',
    border: '1px solid var(--border)', borderRadius: 8,
    background: 'var(--bg)', color: 'var(--text-h)',
    fontSize: 15, boxSizing: 'border-box',
    outline: 'none', transition: 'border-color 0.15s',
  },
  label: {
    display: 'block', fontSize: 13, fontWeight: 500,
    color: 'var(--text)', marginBottom: 6, textAlign: 'left',
  },
}

export default function Login() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({ email: '', password: '' })
  const [showPassword, setShowPassword] = useState(false)
  const [error, setError] = useState('')
  const [isLoading, setIsLoading] = useState(false)

  const set = (field) => (e) => setForm(f => ({ ...f, [field]: e.target.value }))

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setIsLoading(true)
    try {
      await login(form)
      navigate('/dashboard')
    } catch (err) {
      setError(err.response?.data?.message || 'Invalid email or password')
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div style={{
      minHeight: '100svh', display: 'flex', flexDirection: 'column',
      alignItems: 'center', justifyContent: 'center', padding: '1.5rem',
    }}>
      {/* Brand */}
      <div style={{ marginBottom: '2rem', textAlign: 'center' }}>
        <div style={{ fontSize: 36, marginBottom: '0.5rem' }}>💎</div>
        <h1 style={{ margin: 0, fontSize: 28, fontWeight: 700, color: 'var(--text-h)', letterSpacing: '-0.75px' }}>
          Sovereign
        </h1>
        <p style={{ margin: '0.35rem 0 0', fontSize: 14, color: 'var(--text)' }}>
          Your personal finance command center
        </p>
      </div>

      {/* Card */}
      <div style={{
        background: 'var(--bg)', border: '1px solid var(--border)',
        borderRadius: 16, padding: '2rem', width: '100%', maxWidth: 400,
        boxShadow: 'var(--shadow)',
      }}>
        <h2 style={{ margin: '0 0 1.5rem', fontSize: 18, fontWeight: 600, color: 'var(--text-h)', textAlign: 'left' }}>
          Sign in to your account
        </h2>

        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          <div>
            <label style={s.label}>Email</label>
            <input
              type="email"
              value={form.email}
              onChange={set('email')}
              required
              autoComplete="email"
              placeholder="you@example.com"
              style={s.input}
            />
          </div>

          <div>
            <label style={s.label}>Password</label>
            <div style={{ position: 'relative' }}>
              <input
                type={showPassword ? 'text' : 'password'}
                value={form.password}
                onChange={set('password')}
                required
                autoComplete="current-password"
                placeholder="••••••••"
                style={{ ...s.input, paddingRight: '2.75rem' }}
              />
              <button
                type="button"
                onClick={() => setShowPassword(v => !v)}
                style={{
                  position: 'absolute', right: '0.75rem', top: '50%',
                  transform: 'translateY(-50%)', background: 'none',
                  border: 'none', cursor: 'pointer', fontSize: 16,
                  color: 'var(--text)', padding: 0, lineHeight: 1,
                }}
                tabIndex={-1}
              >
                {showPassword ? '🙈' : '👁'}
              </button>
            </div>
          </div>

          {error && (
            <div style={{
              padding: '0.65rem 0.85rem', background: 'rgba(239,68,68,0.08)',
              border: '1px solid rgba(239,68,68,0.2)', borderRadius: 8,
              color: '#ef4444', fontSize: 13, textAlign: 'left',
            }}>
              {error}
            </div>
          )}

          <button
            type="submit"
            disabled={isLoading}
            style={{
              background: 'var(--accent)', color: '#fff', border: 'none',
              borderRadius: 8, padding: '0.7rem', cursor: 'pointer',
              fontSize: 15, fontWeight: 600, marginTop: '0.25rem',
              opacity: isLoading ? 0.75 : 1, transition: 'opacity 0.15s',
            }}
          >
            {isLoading ? 'Signing in...' : 'Sign In'}
          </button>
        </form>

        <p style={{ margin: '1.25rem 0 0', fontSize: 14, color: 'var(--text)', textAlign: 'center' }}>
          Don't have an account?{' '}
          <Link to="/register" style={{ color: 'var(--accent)', fontWeight: 500, textDecoration: 'none' }}>
            Create one
          </Link>
        </p>
      </div>
    </div>
  )
}
