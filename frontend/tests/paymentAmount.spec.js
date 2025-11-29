import { mount } from "@vue/test-utils";
import { describe, it, expect, vi, beforeEach } from "vitest";
import Payment from "@/views/Payment.vue";

vi.mock("@/api/order", () => ({
  default: {
    getOrderByNo: vi.fn(async (orderNo) => ({
      code: 200,
      data: {
        orderNo,
        serviceName: "基础洗车",
        bookingDate: "2025-11-12",
        bookingTime: "16:00-16:30",
        location: "上门服务",
        totalPrice: 30,
      },
    })),
  },
}));

vi.mock("@/api/payment", () => ({
  default: {
    getPublicKey: vi.fn(async () => ({
      code: 200,
      data: "-----BEGIN PUBLIC KEY-----\nMIIB...\n-----END PUBLIC KEY-----",
    })),
    createPayment: vi.fn(async () => ({
      code: 200,
      data: { paymentNo: "PM123" },
    })),
  },
}));

describe("Payment amount sync", () => {
  let wrapper;

  beforeEach(async () => {
    wrapper = mount(Payment, {
      global: {
        mocks: {
          $route: { params: { orderNo: "CW123" }, query: {} },
          $router: { push: vi.fn() },
        },
      },
    });
    await new Promise((r) => setTimeout(r, 0));
  });

  it("maps totalPrice to totalAmount and shows appointmentTime", () => {
    const amountText = wrapper.find(".amount").text();
    expect(amountText).toMatch("¥30");
    const details = wrapper.text();
    expect(details).toMatch("预约时间：");
  });

  it("updates button amount immediately when credit card amount changes", async () => {
    await wrapper.vm.selectPaymentMethod("credit_card");
    const input = wrapper.find('input[type="number"]');
    await input.setValue("45");
    await wrapper.vm.$nextTick();
    const btn = wrapper.find(".btn-pay");
    expect(btn.text()).toMatch("¥45");
  });

  it("clamps negative payAmount to zero", async () => {
    await wrapper.vm.selectPaymentMethod("credit_card");
    const input = wrapper.find('input[type="number"]');
    await input.setValue("-10");
    await wrapper.vm.$nextTick();
    const btn = wrapper.find(".btn-pay");
    expect(btn.text()).toMatch("¥0");
  });
});
