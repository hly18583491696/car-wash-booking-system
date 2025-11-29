import request from "./request";

const publicApi = {
  getStatsSummary() {
    return request.get("/public/stats/summary");
  },
  getLatestFeedback(limit = 6) {
    return request.get("/public/feedback/latest", { params: { limit } });
  },
};

export default publicApi;
