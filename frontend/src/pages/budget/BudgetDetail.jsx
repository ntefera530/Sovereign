import { useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { budgetsApi } from '../../api/budgets'
import { formatCurrency, formatDate, formatPercent } from '../../utils/formatters'

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
  btnDanger: {
    background: 'transparent', color: '#ef4444',
    border: '1px solid rgba(239,68,68,0.4)', borderRadius: 8,
    padding: '0.5rem 1.25rem', cursor: 'pointer', fontSize: 14,
  },
  btnIcon: {
    background: 'transparent', border: 'none', cursor: 'pointer',
    padding: '0.35rem 0.6rem', borderRadius: 6, fontSize: 14,
  },
  input: {
    width: '100%', padding: '0.5rem 0.75rem',
    border: '1px solid var(--border)', borderRadius: 8,
    background: 'var(--bg)', color: 'var(--text-h)',
    fontSize: 14, boxSizing: 'border-box',
  },
  label: { display: 'block', fontSize: 13, fontWeight: 500, color: 'var(--text)', marginBottom: 4 },
  card: {
    background: 'var(--bg)', border: '1px solid var(--border)',
    borderRadius: 12, padding: '1.25rem',
  },
}

function ProgressBar({ percent, isOver, height = 8 }) {
  const clamped = Math.min(parseFloat(percent) || 0, 100)
  const color = isOver ? '#ef4444' : clamped >= 80 ? '#f59e0b' : '#22c55e'
  return (
    <div style={{ background: 'var(--border)', borderRadius: 4, height, overflow: 'hidden', width: '100%' }}>
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
          borderRadius: 16, padding: '1.5rem', width: '100%', maxWidth: 440,
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

function CategoryModal({ budgetId, category, onClose }) {
  const queryClient = useQueryClient()
  const isEdit = !!category
  const [form, setForm] = useState({
    name: category?.name || '',
    limitAmount: category?.limitAmount || '',
  })

  const { mutate, isPending, error } = useMutation({
    mutationFn: (data) =>
      isEdit
        ? budgetsApi.updateCategory(budgetId, category.id, data)
        : budgetsApi.createCategory(budgetId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['budget', budgetId] })
      queryClient.invalidateQueries({ queryKey: ['budget-summary', budgetId] })
      onClose()
    },
  })

  const handleSubmit = (e) => {
    e.preventDefault()
    mutate({ name: form.name, limitAmount: parseFloat(form.limitAmount) })
  }

  return (
    <Modal title={isEdit ? 'Edit Category' : 'Add Category'} onClose={onClose}>
      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
        <div>
          <label style={s.label}>Category Name</label>
          <input
            value={form.name}
            onChange={e => setForm(f => ({ ...f, name: e.target.value }))}
            required
            placeholder="e.g. Food & Dining"
            style={s.input}
          />
        </div>
        <div>
          <label style={s.label}>Spending Limit ($)</label>
          <input
            type="number"
            min="0.01"
            step="0.01"
            value={form.limitAmount}
            onChange={e => setForm(f => ({ ...f, limitAmount: e.target.value }))}
            required
            placeholder="0.00"
            style={s.input}
          />
        </div>
        {error && (
          <p style={{ color: '#ef4444', fontSize: 13, margin: 0 }}>
            {error.response?.data?.message || 'Something went wrong'}
          </p>
        )}
        <div style={{ display: 'flex', gap: '0.5rem', justifyContent: 'flex-end', marginTop: '0.25rem' }}>
          <button type="button" onClick={onClose} style={s.btnSecondary}>Cancel</button>
          <button type="submit" disabled={isPending} style={{ ...s.btnPrimary, opacity: isPending ? 0.7 : 1 }}>
            {isPending ? 'Saving...' : isEdit ? 'Save Changes' : 'Add Category'}
          </button>
        </div>
      </form>
    </Modal>
  )
}

function EditBudgetModal({ budget, onClose }) {
  const queryClient = useQueryClient()
  const [form, setForm] = useState({
    name: budget.name,
    endDate: budget.endDate || '',
  })

  const { mutate, isPending, error } = useMutation({
    mutationFn: (data) => budgetsApi.update(budget.id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['budget', budget.id] })
      queryClient.invalidateQueries({ queryKey: ['budgets'] })
      onClose()
    },
  })

  const handleSubmit = (e) => {
    e.preventDefault()
    const payload = { name: form.name }
    if (form.endDate) payload.endDate = form.endDate
    mutate(payload)
  }

  return (
    <Modal title="Edit Budget" onClose={onClose}>
      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
        <div>
          <label style={s.label}>Budget Name</label>
          <input
            value={form.name}
            onChange={e => setForm(f => ({ ...f, name: e.target.value }))}
            required
            style={s.input}
          />
        </div>
        <div>
          <label style={s.label}>End Date <span style={{ fontWeight: 400 }}>(optional)</span></label>
          <input
            type="date"
            value={form.endDate}
            onChange={e => setForm(f => ({ ...f, endDate: e.target.value }))}
            style={s.input}
          />
        </div>
        {error && (
          <p style={{ color: '#ef4444', fontSize: 13, margin: 0 }}>
            {error.response?.data?.message || 'Failed to update budget'}
          </p>
        )}
        <div style={{ display: 'flex', gap: '0.5rem', justifyContent: 'flex-end', marginTop: '0.25rem' }}>
          <button type="button" onClick={onClose} style={s.btnSecondary}>Cancel</button>
          <button type="submit" disabled={isPending} style={{ ...s.btnPrimary, opacity: isPending ? 0.7 : 1 }}>
            {isPending ? 'Saving...' : 'Save Changes'}
          </button>
        </div>
      </form>
    </Modal>
  )
}

function ConfirmModal({ title, message, onConfirm, onClose, isPending }) {
  return (
    <Modal title={title} onClose={onClose}>
      <p style={{ color: 'var(--text)', fontSize: 14, margin: '0 0 1.5rem' }}>{message}</p>
      <div style={{ display: 'flex', gap: '0.5rem', justifyContent: 'flex-end' }}>
        <button onClick={onClose} style={s.btnSecondary}>Cancel</button>
        <button
          onClick={onConfirm}
          disabled={isPending}
          style={{ background: '#ef4444', color: '#fff', border: 'none', borderRadius: 8, padding: '0.5rem 1.25rem', cursor: 'pointer', fontSize: 14, opacity: isPending ? 0.7 : 1 }}
        >
          {isPending ? 'Deleting...' : 'Delete'}
        </button>
      </div>
    </Modal>
  )
}

function StatCard({ label, value, sub, accent }) {
  return (
    <div style={{ ...s.card, textAlign: 'center' }}>
      <p style={{ margin: '0 0 0.25rem', fontSize: 12, color: 'var(--text)', fontWeight: 500, textTransform: 'uppercase', letterSpacing: '0.05em' }}>
        {label}
      </p>
      <p style={{ margin: 0, fontSize: 22, fontWeight: 700, color: accent || 'var(--text-h)' }}>{value}</p>
      {sub && <p style={{ margin: '0.25rem 0 0', fontSize: 12, color: 'var(--text)' }}>{sub}</p>}
    </div>
  )
}

function CategoryRow({ category, budgetId, onEdit }) {
  const queryClient = useQueryClient()
  const [showConfirm, setShowConfirm] = useState(false)

  const { mutate: del, isPending } = useMutation({
    mutationFn: () => budgetsApi.deleteCategory(budgetId, category.id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['budget', budgetId] })
      queryClient.invalidateQueries({ queryKey: ['budget-summary', budgetId] })
    },
  })

  const pct = parseFloat(category.percentageUsed) || 0
  const isOver = category.isOverBudget
  const isApproaching = category.isApproachingLimit

  return (
    <>
      <div style={{
        ...s.card,
        padding: '1rem 1.25rem',
        borderLeft: `3px solid ${isOver ? '#ef4444' : isApproaching ? '#f59e0b' : 'var(--accent)'}`,
      }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '0.6rem' }}>
          <div>
            <p style={{ margin: 0, fontWeight: 600, fontSize: 15, color: 'var(--text-h)' }}>{category.name}</p>
            <p style={{ margin: '0.1rem 0 0', fontSize: 12, color: 'var(--text)' }}>
              {formatCurrency(category.spentAmount)} / {formatCurrency(category.limitAmount)}
              {' · '}
              <span style={{ color: isOver ? '#ef4444' : 'var(--text)' }}>
                {isOver
                  ? `${formatCurrency(Math.abs(category.remainingAmount))} over`
                  : `${formatCurrency(category.remainingAmount)} left`}
              </span>
            </p>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
            <span style={{ fontSize: 13, fontWeight: 600, color: isOver ? '#ef4444' : isApproaching ? '#f59e0b' : 'var(--text)' }}>
              {formatPercent(pct)}
            </span>
            <button onClick={() => onEdit(category)} style={{ ...s.btnIcon, color: 'var(--text)' }} title="Edit">✏️</button>
            <button onClick={() => setShowConfirm(true)} style={{ ...s.btnIcon, color: '#ef4444' }} title="Delete">🗑</button>
          </div>
        </div>
        <ProgressBar percent={pct} isOver={isOver} height={6} />
        {(isOver || isApproaching) && (
          <p style={{ margin: '0.5rem 0 0', fontSize: 12, color: isOver ? '#ef4444' : '#f59e0b', fontWeight: 500 }}>
            {isOver ? '⚠ Over budget' : '⚡ Approaching limit'}
          </p>
        )}
      </div>

      {showConfirm && (
        <ConfirmModal
          title="Delete Category"
          message={`Delete "${category.name}"? This cannot be undone.`}
          onConfirm={del}
          onClose={() => setShowConfirm(false)}
          isPending={isPending}
        />
      )}
    </>
  )
}

export default function BudgetDetail() {
  const { id } = useParams()
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  const [showAddCategory, setShowAddCategory] = useState(false)
  const [editingCategory, setEditingCategory] = useState(null)
  const [showEditBudget, setShowEditBudget] = useState(false)
  const [showDeleteBudget, setShowDeleteBudget] = useState(false)

  const { data: budget, isLoading, error } = useQuery({
    queryKey: ['budget', id],
    queryFn: () => budgetsApi.getOne(id).then(r => r.data),
  })

  const { data: summary } = useQuery({
    queryKey: ['budget-summary', id],
    queryFn: () => budgetsApi.getSummary(id).then(r => r.data),
    enabled: !!budget,
  })

  const { mutate: deleteBudget, isPending: isDeleting } = useMutation({
    mutationFn: () => budgetsApi.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['budgets'] })
      navigate('/budgets')
    },
  })

  if (isLoading) {
    return (
      <div style={{ padding: '2rem', textAlign: 'center', color: 'var(--text)' }}>
        Loading budget...
      </div>
    )
  }

  if (error || !budget) {
    return (
      <div style={{ padding: '2rem' }}>
        <button onClick={() => navigate('/budgets')} style={s.btnSecondary}>← Back</button>
        <p style={{ color: '#ef4444', marginTop: '1rem', fontSize: 14 }}>
          Budget not found or failed to load.
        </p>
      </div>
    )
  }

  const totalPct = parseFloat(summary?.percentageUsed || 0)
  const isOver = parseFloat(budget.totalSpent) > parseFloat(budget.totalLimit)

  return (
    <div style={{ padding: '2rem', textAlign: 'left', maxWidth: 960, margin: '0 auto' }}>
      {/* Header */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '1.75rem', flexWrap: 'wrap' }}>
        <button
          onClick={() => navigate('/budgets')}
          style={{ ...s.btnSecondary, padding: '0.4rem 0.9rem', fontSize: 13 }}
        >
          ← Back
        </button>
        <div style={{ flex: 1 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', flexWrap: 'wrap' }}>
            <h1 style={{ margin: 0, fontSize: 24, fontWeight: 700, color: 'var(--text-h)', letterSpacing: '-0.5px' }}>
              {budget.name}
            </h1>
            <span style={{
              fontSize: 11, fontWeight: 600, padding: '3px 10px', borderRadius: 99,
              background: budget.isActive ? 'rgba(34,197,94,0.12)' : 'rgba(107,99,117,0.1)',
              color: budget.isActive ? '#16a34a' : 'var(--text)',
              textTransform: 'uppercase', letterSpacing: '0.05em',
            }}>
              {budget.isActive ? 'Active' : 'Inactive'}
            </span>
          </div>
          <p style={{ margin: '0.2rem 0 0', fontSize: 13, color: 'var(--text)' }}>
            {budget.periodType.charAt(0) + budget.periodType.slice(1).toLowerCase()}
            {budget.startDate && ` · ${formatDate(budget.startDate)}`}
            {budget.endDate && ` – ${formatDate(budget.endDate)}`}
          </p>
        </div>
        <div style={{ display: 'flex', gap: '0.5rem' }}>
          <button onClick={() => setShowEditBudget(true)} style={s.btnSecondary}>Edit</button>
          <button onClick={() => setShowDeleteBudget(true)} style={s.btnDanger}>Delete</button>
        </div>
      </div>

      {/* Summary Cards */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(160px, 1fr))', gap: '1rem', marginBottom: '1.5rem' }}>
        <StatCard label="Total Limit" value={formatCurrency(budget.totalLimit)} />
        <StatCard
          label="Total Spent"
          value={formatCurrency(budget.totalSpent)}
          accent={isOver ? '#ef4444' : undefined}
        />
        <StatCard
          label="Remaining"
          value={formatCurrency(Math.abs(budget.totalRemaining))}
          sub={isOver ? 'over budget' : 'available'}
          accent={isOver ? '#ef4444' : '#22c55e'}
        />
        {summary && (
          <StatCard
            label="Used"
            value={formatPercent(totalPct)}
            sub={`${summary.categoriesOverBudget || 0} over limit`}
            accent={totalPct >= 100 ? '#ef4444' : totalPct >= 80 ? '#f59e0b' : undefined}
          />
        )}
      </div>

      {/* Overall Progress */}
      <div style={{ ...s.card, marginBottom: '1.5rem' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.6rem' }}>
          <p style={{ margin: 0, fontWeight: 600, fontSize: 14, color: 'var(--text-h)' }}>Overall Progress</p>
          <span style={{ fontSize: 14, fontWeight: 600, color: isOver ? '#ef4444' : 'var(--text)' }}>
            {formatPercent(totalPct)}
          </span>
        </div>
        <ProgressBar percent={totalPct} isOver={isOver} height={12} />
        <p style={{ margin: '0.5rem 0 0', fontSize: 13, color: 'var(--text)' }}>
          {formatCurrency(budget.totalSpent)} spent of {formatCurrency(budget.totalLimit)} total limit
        </p>
      </div>

      {/* AI Insight */}
      {summary?.insight && (
        <div style={{
          background: 'var(--accent-bg)', border: '1px solid var(--accent-border)',
          borderRadius: 10, padding: '1rem 1.25rem', marginBottom: '1.5rem',
          display: 'flex', gap: '0.75rem', alignItems: 'flex-start',
        }}>
          <span style={{ fontSize: 18 }}>💡</span>
          <p style={{ margin: 0, fontSize: 14, color: 'var(--text-h)', lineHeight: 1.6 }}>{summary.insight}</p>
        </div>
      )}

      {/* Categories */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
        <h2 style={{ margin: 0, fontSize: 18, fontWeight: 600, color: 'var(--text-h)' }}>
          Categories
          {budget.categories?.length > 0 && (
            <span style={{ fontSize: 13, fontWeight: 400, color: 'var(--text)', marginLeft: '0.5rem' }}>
              ({budget.categories.length})
            </span>
          )}
        </h2>
        <button onClick={() => setShowAddCategory(true)} style={s.btnPrimary}>
          + Add Category
        </button>
      </div>

      {budget.categories?.length === 0 && (
        <div style={{ ...s.card, textAlign: 'center', padding: '3rem' }}>
          <p style={{ margin: '0 0 0.5rem', fontSize: 16, fontWeight: 500, color: 'var(--text-h)' }}>No categories yet</p>
          <p style={{ margin: '0 0 1.25rem', fontSize: 14, color: 'var(--text)' }}>
            Add spending categories to track where your money goes.
          </p>
          <button onClick={() => setShowAddCategory(true)} style={s.btnPrimary}>
            Add your first category
          </button>
        </div>
      )}

      {budget.categories?.length > 0 && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
          {budget.categories.map(cat => (
            <CategoryRow
              key={cat.id}
              category={cat}
              budgetId={id}
              onEdit={setEditingCategory}
            />
          ))}
        </div>
      )}

      {/* Modals */}
      {showAddCategory && (
        <CategoryModal budgetId={id} onClose={() => setShowAddCategory(false)} />
      )}
      {editingCategory && (
        <CategoryModal budgetId={id} category={editingCategory} onClose={() => setEditingCategory(null)} />
      )}
      {showEditBudget && (
        <EditBudgetModal budget={budget} onClose={() => setShowEditBudget(false)} />
      )}
      {showDeleteBudget && (
        <ConfirmModal
          title="Delete Budget"
          message={`Delete "${budget.name}" and all its categories? This cannot be undone.`}
          onConfirm={deleteBudget}
          onClose={() => setShowDeleteBudget(false)}
          isPending={isDeleting}
        />
      )}
    </div>
  )
}
