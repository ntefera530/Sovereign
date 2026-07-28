import { useCallback, useEffect, useState } from 'react'
import { usePlaidLink } from 'react-plaid-link'
import { plaidApi } from '../../api/plaid'

export default function PlaidLinkButton({ onSuccess }) {
  const [linkToken, setLinkToken] = useState(null)

  useEffect(() => {
    plaidApi.getLinkToken().then((res) => setLinkToken(res.data.linkToken))
  }, [])

  const onPlaidSuccess = useCallback(
    async (publicToken) => {
      await plaidApi.exchangePublicToken(publicToken)
      onSuccess?.()
    },
    [onSuccess]
  )

  const { open, ready } = usePlaidLink({
    token: linkToken,
    onSuccess: onPlaidSuccess,
  })

  return (
    <button onClick={() => open()} disabled={!ready}>
      Connect a bank account
    </button>
  )
}