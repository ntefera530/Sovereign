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

export default function Register() {
  const { register } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({ firstName: '', lastName: '', email: '', password: '' })
  const [showPassword, setShowPassword] = useState(false)
  const [error, setError] = useState('')
  const [fieldErrors, setFieldErrors] = useState({})
  const [isLoading, setIsLoading] = useState(false)

  const set = (field) => (e) => {
    setForm(f => ({ ...f, [field]: e.target.value }))
    if (fieldErrors[field]) setFieldErrors(fe => ({ ...fe, [field]: '' }))
  }

  const validate = () => {
    const errs = {}
    if (!form.firstName.trim()) errs.firstName = 'First name is required'
    if (!form.lastName.trim()) errs.lastName = 'Last name is required'
    if (!form.email.trim()) errs.email = 'Email is required'
    if (form.password.length < 8) errs.password = 'Password must be at least 8 characters'
    return errs
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    const errs = validate()
    if (Object.keys(errs).length > 0) { setFieldErrors(errs); return }
    setIsLoading(true)
    try {
      await register(form)
      navigate('/dashboard')
    } catch (err) {
      const msg = err.response?.data?.message
      if (msg?.toLowerCase().includes('email')) {
        setFieldErrors({ email: msg })
      } else {
        setError(msg || 'Registration failed. Please try again.')
      }
    } finally {
      setIsLoading(false)
    }
  }

  const Field = ({ id, label, children }) => (
    <div>
      <label style={s.label}>{label}</label>
      {children}
      {fieldErrors[id] && (
        <p style={{ margin: '4px 0 0', fontSize: 12, color: '#ef4444', textAlign: 'left' }}>
          {fieldErrors[id]}
        </p>
      )}
    </div>
  )

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
          Create your account
        </h2>

        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem' }}>
            <Field id="firstName" label="First Name">
              <input
                value={form.firstName}
                onChange={set('firstName')}
                required
                autoComplete="given-name"
                placeholder="Jane"
                style={{
                  ...s.input,
                  borderColor: fieldErrors.firstName ? 'rgba(239,68,68,0.5)' : 'var(--border)',
                }}
              />
            </Field>
            <Field id="lastName" label="Last Name">
              <input
                value={form.lastName}
                onChange={set('lastName')}
                required
                autoComplete="family-name"
                placeholder="Doe"
                style={{
                  ...s.input,
                  borderColor: fieldErrors.lastName ? 'rgba(239,68,68,0.5)' : 'var(--border)',
                }}
              />
            </Field>
          </div>

          <Field id="email" label="Email">
            <input
              type="email"
              value={form.email}
              onChange={set('email')}
              required
              autoComplete="email"
              placeholder="you@example.com"
              style={{
                ...s.input,
                borderColor: fieldErrors.email ? 'rgba(239,68,68,0.5)' : 'var(--border)',
              }}
            />
          </Field>

          <Field id="password" label="Password">
            <div style={{ position: 'relative' }}>
              <input
                type={showPassword ? 'text' : 'password'}
                value={form.password}
                onChange={set('password')}
                required
                autoComplete="new-password"
                placeholder="Min. 8 characters"
                style={{
                  ...s.input,
                  paddingRight: '2.75rem',
                  borderColor: fieldErrors.password ? 'rgba(239,68,68,0.5)' : 'var(--border)',
                }}
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
            {form.password && (
              <PasswordStrength password={form.password} />
            )}
          </Field>

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
            {isLoading ? 'Creating account...' : 'Create Account'}
          </button>
        </form>

        <p style={{ margin: '1.25rem 0 0', fontSize: 14, color: 'var(--text)', textAlign: 'center' }}>
          Already have an account?{' '}
          <Link to="/login" style={{ color: 'var(--accent)', fontWeight: 500, textDecoration: 'none' }}>
            Sign in
          </Link>
        </p>
      </div>
    </div>
  )
}

function PasswordStrength({ password }) {
  const checks = [
    password.length >= 8,
    /[A-Z]/.test(password),
    /[0-9]/.test(password),
    /[^A-Za-z0-9]/.test(password),
  ]
  const score = checks.filter(Boolean).length
  const label = ['Too short', 'Weak', 'Fair', 'Good', 'Strong'][score]
  const color = ['#ef4444', '#ef4444', '#f59e0b', '#22c55e', '#16a34a'][score]

  return (
    <div style={{ marginTop: 8 }}>
      <div style={{ display: 'flex', gap: 4, marginBottom: 4 }}>
        {[0, 1, 2, 3].map(i => (
          <div
            key={i}
            style={{
              flex: 1, height: 3, borderRadius: 2,
              background: i < score ? color : 'var(--border)',
              transition: 'background 0.2s',
            }}
          />
        ))}
      </div>
      <p style={{ margin: 0, fontSize: 12, color, textAlign: 'left' }}>{label}</p>
    </div>
  )
}
