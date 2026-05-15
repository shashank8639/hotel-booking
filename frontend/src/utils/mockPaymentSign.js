/**
 * Demo-only HMAC helper for Mock Razorpay verify.
 * Production: Razorpay Checkout returns a signature; never ship real secrets in the SPA.
 *
 * Must match backend MockRazorpayGateway default secret when keySecret is blank.
 */
const MOCK_SECRET = 'mock_razorpay_secret';

function toHex(buffer) {
  return [...new Uint8Array(buffer)].map((b) => b.toString(16).padStart(2, '0')).join('');
}

/**
 * Signs orderId|paymentId the same way Razorpay payment verification expects.
 */
export async function signMockPayment(orderId, paymentId) {
  const key = await crypto.subtle.importKey(
    'raw',
    new TextEncoder().encode(MOCK_SECRET),
    { name: 'HMAC', hash: 'SHA-256' },
    false,
    ['sign']
  );
  const mac = await crypto.subtle.sign(
    'HMAC',
    key,
    new TextEncoder().encode(`${orderId}|${paymentId}`)
  );
  return toHex(mac);
}

export function isMockOrder(orderId) {
  return typeof orderId === 'string' && orderId.startsWith('order_mock_');
}
