import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import CheckoutOrderSummary from '../components/CheckoutOrderSummary.jsx'
import CheckoutSteps from '../components/CheckoutSteps.jsx'
import SiteFooter from '../components/SiteFooter.jsx'
import { useShop } from '../context/ShopContext.jsx'
import {
  calculateCheckoutTotals,
  clearCheckoutItemIds,
  createOrderNumber,
  deliveryEstimate,
  deliveryLabel,
  getCheckoutDetails,
  getCheckoutItems,
  setLastOrder
} from '../utils/checkoutSession.js'

const paymentOptions = [
  { id: 'card', title: 'Credit / Debit Card', description: 'Visa, Mastercard and secure card payments', mark: '▣' },
  { id: 'wallet', title: 'JazzCash / Easypaisa', description: 'Pay securely with your mobile wallet', mark: '◉' },
  { id: 'bank', title: 'Bank Transfer', description: 'Direct bank transfer to our account', mark: '▤' },
  { id: 'cod', title: 'Cash on Delivery', description: 'Pay when you receive your order', mark: '▧' }
]

export default function PaymentPage() {
  const navigate = useNavigate()
  const { cart, coupon, applyCoupon, removeFromCart } = useShop()
  const [method, setMethod] = useState('card')
  const [couponInput, setCouponInput] = useState('')
  const [couponMessage, setCouponMessage] = useState('')
  const [card, setCard] = useState({ number: '', expiry: '', cvv: '', holder: '' })
  const [billingSame, setBillingSame] = useState(true)
  const [error, setError] = useState('')
  const details = getCheckoutDetails()
  const items = getCheckoutItems(cart)
  const totals = calculateCheckoutTotals(items, details.delivery, coupon)

  function applyCheckoutCoupon(event) {
    event.preventDefault()
    const valid = applyCoupon(couponInput)
    setCouponMessage(valid ? '10% discount applied to this order.' : 'Try HUNAR10 or WELCOME10.')
  }

  function updateCard(event) {
    const { name, value } = event.target
    setCard((current) => ({ ...current, [name]: value }))
    setError('')
  }

  function placeOrder(event) {
    event.preventDefault()
    if (items.length === 0) {
      setError('Your selected cart items are no longer available. Return to the cart to continue.')
      return
    }
    if (!details.fullName?.trim() || !details.phone?.trim() || !details.address?.trim() || !details.city?.trim() || !details.province?.trim()) {
      setError('Add your shipping address before placing the order.')
      return
    }
    if (method === 'card' && (!card.number.trim() || !card.expiry.trim() || !card.cvv.trim() || !card.holder.trim())) {
      setError('Enter the card details or choose another payment method.')
      return
    }

    const selectedOption = paymentOptions.find((option) => option.id === method)
    const order = {
      number: createOrderNumber(),
      items,
      itemCount: items.reduce((sum, item) => sum + item.quantity, 0),
      totals,
      paymentMethod: selectedOption?.title || 'Payment method',
      delivery: deliveryLabel(details.delivery),
      estimatedDelivery: deliveryEstimate(details.delivery),
      address: details
    }
    setLastOrder(order)
    items.forEach((item) => removeFromCart(item.id))
    clearCheckoutItemIds()
    navigate('/order-confirmed')
  }

  return (
    <main className="checkout-route-page page-shell">
      <section className="checkout-hero payment-hero">
        <div>
          <h1>Payment</h1>
          <p>Home &nbsp;›&nbsp; Cart &nbsp;›&nbsp; Checkout &nbsp;›&nbsp; Payment</p>
        </div>
      </section>

      <CheckoutSteps active="payment" />

      {items.length === 0 ? (
        <section className="checkout-empty-state">
          <h2>Your selected items are not available.</h2>
          <p>Return to your cart, choose the handmade pieces you want, then continue to payment.</p>
          <Link to="/cart">Return to Cart →</Link>
        </section>
      ) : (
        <section className="checkout-layout payment-layout">
          <form className="checkout-panel payment-panel" onSubmit={placeOrder}>
            <div className="checkout-panel-heading">
              <h2>Select Payment Method</h2>
              <p>Choose the option that suits you best</p>
            </div>

            <div className="payment-options">
              {paymentOptions.map((option) => (
                <label className={`payment-method-card ${method === option.id ? 'selected' : ''}`} key={option.id}>
                  <input type="radio" value={option.id} checked={method === option.id} onChange={() => { setMethod(option.id); setError('') }} />
                  <span className="payment-method-mark">{option.mark}</span>
                  <span><strong>{option.title}</strong><small>{option.description}</small></span>
                  {option.id === 'card' && <span className="card-brand-row"><b>VISA</b><b>MC</b><b>▣</b></span>}
                </label>
              ))}
            </div>

            {method === 'card' && (
              <div className="card-details-grid">
                <label className="checkout-field full-field">Card Number
                  <input name="number" inputMode="numeric" value={card.number} onChange={updateCard} placeholder="1234 5678 9012 3456" />
                </label>
                <label className="checkout-field">Expiry Date
                  <input name="expiry" value={card.expiry} onChange={updateCard} placeholder="MM/YY" />
                </label>
                <label className="checkout-field">CVV
                  <input name="cvv" inputMode="numeric" value={card.cvv} onChange={updateCard} placeholder="123" />
                </label>
                <label className="checkout-field full-field">Cardholder Name
                  <input name="holder" value={card.holder} onChange={updateCard} placeholder="Enter name on card" />
                </label>
              </div>
            )}

            <label className="save-address-check billing-check"><input type="checkbox" checked={billingSame} onChange={(event) => setBillingSame(event.target.checked)} /> Billing address same as shipping address</label>
            {error && <p className="checkout-error" role="alert">{error}</p>}
            <button className="checkout-continue-button" type="submit">Continue to Review <span>→</span></button>
            <p className="secure-payment-note">▣ Your payment information is secure and encrypted.</p>
          </form>

          <div className="checkout-right-column">
            <CheckoutOrderSummary items={items} totals={totals} />
            <section className="checkout-coupon-card">
              <h3>Have a Coupon Code?</h3>
              <form onSubmit={applyCheckoutCoupon}>
                <input value={couponInput} onChange={(event) => setCouponInput(event.target.value)} placeholder="Enter coupon code" />
                <button type="submit">Apply</button>
              </form>
              {couponMessage && <p>{couponMessage}</p>}
            </section>
          </div>
        </section>
      )}

      <SiteFooter />
    </main>
  )
}
