import { useEffect, useState } from 'react'
import { accountsApi } from '../../api/accounts'
import PlaidLinkButton from '../../components/plaid/PlaidLinkButton'

const Accounts = () => {
  const [summary, setSummary] = useState(null)

  const loadAccounts = () => {
    accountsApi.getAll().then((res) => setSummary(res.data))
  }

  useEffect(() => { loadAccounts() }, [])

  return (
    <div>
      <PlaidLinkButton onSuccess={loadAccounts} />
      {summary?.accounts.map((a) => (
        <div key={a.id}>{a.name} — {a.balance}</div>
      ))}
    </div>
  )
}

export default Accounts