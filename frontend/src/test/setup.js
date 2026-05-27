import '@testing-library/jest-dom/vitest';

// Recharts ResponsiveContainer needs ResizeObserver in jsdom
class ResizeObserverMock {
  observe() {}
  unobserve() {}
  disconnect() {}
}
global.ResizeObserver = global.ResizeObserver || ResizeObserverMock;
