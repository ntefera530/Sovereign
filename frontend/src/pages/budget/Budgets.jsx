import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { budgetsApi } from '../../api/budgets'
import { formatCurrency } from '../../utils/formatters'

const PERIOD_TYPES = ['WEEKLY', 'BIWEEKLY', 'MONTHLY', 'QUARTERLY', 'YEARLY', 'CUSTOM']

const s = {
  btnPrimary: {
    background: 'var(--accent)', color: '#fff', border: 'none',
    borderRadius: 8, padding: '0.5rem 1.25rem', cursor: 'pointer',
    fontSize: 14, fontWeight: 500,
  },
  btnSecondary: {
    background: 'transparent', color: 'var(--text)',
    border: '1px solid var(--border)', borderRadius: 8,
    padding: '0.5rem 1.25rem', cursor: 'pointer', fontSize: 14,
  },
  input: {
    width: '100%', padding: '0.5rem 0.75rem',
    border: '1px solid var(--border)', borderRadius: 8,
    background: 'var(--bg)', color: 'var(--text-h)',
    fontSize: 14, boxSizing: 'border-box',
  },
  label: { display: 'block', fontSize: 13, fontWeight: 500, color: 'var(--text)', marginBottom: 4 },
}

function ProgressBar({ percent, isOver }) {
  const clamped = Math.min(parseFloat(percent) || 0, 100)
  const color = isOver ? '#ef4444' : clamped >= 80 ? '#f59e0b' : '#22c55e'
  return (
    <div style={{ background: 'var(--border)', borderRadius: 4, height: 8, overflow: 'hidden', width: '100%' }}>
      <div style={{ width: `${clamped}%`, background: color, height: '100%', transition: 'width 0.4s ease' }} />
    </div>
  )
}

function Modal({ title, onClose, children }) {
  return (
    <div
      onClick={onClose}
      style={{
        position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.5)',
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        zIndex: 50, padding: '1rem',
      }}
    >
      <div
        onClick={e => e.stopPropagation()}
        style={{
          background: 'var(--bg)', border: '1px solid var(--border)',
          borderRadius: 16, padding: '1.5rem', width: '100%', maxWidth: 480,
          boxShadow: 'var(--shadow)', textAlign: 'left',
        }}
      >
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem' }}>
          <h2 style={{ margin: 0, fontSize: 18, fontWeight: 600, color: 'var(--text-h)' }}>{title}</h2>
          <button
            onClick={onClose}
            style={{ background: 'none', border: 'none', cursor: 'pointer', fontSize: 22, color: 'var(--text)', lineHeight: 1 }}
          >
            ×
          </button>
        </div>
        {children}
      </div>
    </div>
  )
}

function CreateBudgetModal({ onClose }) {
  const queryClient = useQueryClient()
  const today = new Date().toISOString().split('T')[0]
  const [form, setForm] = useState({ name: '', periodType: 'MONTHLY', startDate: today, endDate: '' })

  const { mutate, isPending, error } = useMutation({
    mutationFn: (data) => budgetsApi.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['budgets'] })
      onClose()
    },
  })

  const set = (field) => (e) => setForm(f => ({ ...f, [field]: e.target.value }))

  const handleSubmit = (e) => {
    e.preventDefault()
    const payload = { name: form.name, periodType: form.periodType, startDate: form.startDate }
    if (form.endDate) payload.endDate = form.endDate
    mutate(payload)
  }

  return (
    <Modal title="New Budget" onClose={onClose}>
      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
        <div>
          <label style={s.label}>Name</label>
          <input value={form.name} onChange={set('name')} required placeholder="e.g. Monthly Budget" style={s.input} />
        </div>
        <div>
          <label style={s.label}>Period</label>
          <select value={form.periodType} onChange={set('periodType')} style={s.input}>
            {PERIOD_TYPES.map(t => <option key={t} value={t}>{t.charAt(0) + t.slice(1).toLowerCase()}</option>)}
          </select>
        </div>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem' }}>
          <div>
            <label style={s.label}>Start Date</label>
            <input type="date" value={form.startDate} onChange={set('startDate')} required style={s.input} />
          </div>
          <div>
            <label style={s.label}>End Date <span style={{ fontWeight: 400 }}>(optional)</span></label>
            <input type="date" value={form.endDate} onChange={set('endDate')} style={s.input} />
          </div>
        </div>
        {error && (
          <p style={{ color: '#ef4444', fontSize: 13, margin: 0 }}>
            {error.response?.data?.message || 'Failed to create budget'}
          </p>
        )}
        <div style={{ display: 'flex', gap: '0.5rem', justifyContent: 'flex-end', marginTop: '0.5rem' }}>
          <button type="button" onClick={onClose} style={s.btnSecondary}>Cancel</button>
          <button type="submit" disabled={isPending} style={{ ...s.btnPrimary, opacity: isPending ? 0.7 : 1 }}>
            {isPending ? 'Creating...' : 'Create Budget'}
          </button>
        </div>
      </form>
    </Modal>
  )
}

function BudgetCard({ budget }) {
  const [hovered, setHovered] = useState(false)
  const spent = parseFloat(budget.totalSpent) || 0
  const limit = parseFloat(budget.totalLimit) || 0
  const percent = limit > 0 ? (spent / limit) * 100 : 0
  const isOver = spent > limit

  return (
    <Link to={`/budgets/${budget.id}`} style={{ textDecoration: 'none' }}>
      <div
        onMouseEnter={() => setHovered(true)}
        onMouseLeave={() => setHovered(false)}
        style={{
          background: 'var(--bg)',
          border: `1px solid ${hovered ? 'var(--accent-border)' : 'var(--border)'}`,
          borderRadius: 12, padding: '1.25rem', textAlign: 'left',
          transition: 'border-color 0.15s, box-shadow 0.15s',
          boxShadow: hovered ? 'var(--shadow)' : 'none',
        }}
      >
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '0.75rem' }}>
          <div>
            <h3 style={{ margin: 0, fontSize: 16, fontWeight: 600, color: 'var(--text-h)' }}>{budget.name}</h3>
            <span style={{ fontSize: 12, color: 'var(--text)' }}>
              {budget.periodType.charAt(0) + budget.periodType.slice(1).toLowerCase()}
            </span>
          </div>
          <span style={{
            fontSize: 11, fontWeight: 600, padding: '3px 10px', borderRadius: 99,
            background: budget.isActive ? 'rgba(34,197,94,0.12)' : 'rgba(107,99,117,0.1)',
            color: budget.isActive ? '#16a34a' : 'var(--text)',
            textTransform: 'uppercase', letterSpacing: '0.05em',
          }}>
            {budget.isActive ? 'Active' : 'Inactive'}
          </span>
        </div>

        <div style={{ marginBottom: '0.5rem' }}>
          <ProgressBar percent={percent} isOver={isOver} />
        </div>

        <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 13 }}>
          <span style={{ color: 'var(--text)' }}>
            <strong style={{ color: 'var(--text-h)' }}>{formatCurrency(spent)}</strong>
            {' / '}{formatCurrency(limit)}
          </span>
          <span style={{ color: isOver ? '#ef4444' : 'var(--text)', fontWeight: isOver ? 600 : 400 }}>
            {isOver
              ? `${formatCurrency(Math.abs(budget.totalRemaining))} over`
              : `${formatCurrency(budget.totalRemaining)} left`}
          </span>
        </div>

        {budget.categories?.length > 0 && (
          <p style={{ margin: '0.5rem 0 0', fontSize: 12, color: 'var(--text)' }}>
            {budget.categories.length} categor{budget.categories.length === 1 ? 'y' : 'ies'}
          </p>
        )}
      </div>
    </Link>
  )
}

export default function Budgets() {
  const [showCreate, setShowCreate] = useState(false)

  const { data: budgets, isLoading, error } = useQuery({
    queryKey: ['budgets'],
    queryFn: () => budgetsApi.getAll().then(r => r.data),
  })

  return (
    <div style={{ padding: '2rem', textAlign: 'left', maxWidth: 960, margin: '0 auto' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.75rem' }}>
        <div>
          <h1 style={{ margin: 0, fontSize: 26, fontWeight: 700, color: 'var(--text-h)', letterSpacing: '-0.5px' }}>
            Budgets
          </h1>
          <p style={{ margin: '0.25rem 0 0', color: 'var(--text)', fontSize: 14 }}>
            Track and manage your spending limits
          </p>
        </div>
        <button onClick={() => setShowCreate(true)} style={s.btnPrimary}>
          + New Budget
        </button>
      </div>

      {isLoading && (
        <div style={{ textAlign: 'center', padding: '4rem', color: 'var(--text)' }}>Loading budgets...</div>
      )}

      {error && (
        <div style={{
          padding: '1rem 1.25rem', background: 'rgba(239,68,68,0.08)',
          border: '1px solid rgba(239,68,68,0.2)', borderRadius: 10,
          color: '#ef4444', fontSize: 14,
        }}>
          Failed to load budgets — make sure the backend is running on port 8080.
        </div>
      )}

      {!isLoading && budgets?.length === 0 && (
        <div style={{ textAlign: 'center', padding: '5rem 2rem' }}>
          <div style={{ fontSize: 52, marginBottom: '0.75rem' }}>💰</div>
          <p style={{ fontSize: 18, fontWeight: 600, color: 'var(--text-h)', margin: '0 0 0.5rem' }}>No budgets yet</p>
          <p style={{ fontSize: 14, color: 'var(--text)', margin: '0 0 1.5rem' }}>
            Create your first budget to start tracking spending.
          </p>
          <button onClick={() => setShowCreate(true)} style={s.btnPrimary}>
            Create your first budget
          </button>
        </div>
      )}

      {budgets && budgets.length > 0 && (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))', gap: '1rem' }}>
          {budgets.map(b => <BudgetCard key={b.id} budget={b} />)}
        </div>
      )}

      {showCreate && <CreateBudgetModal onClose={() => setShowCreate(false)} />}
    </div>
  )
}
