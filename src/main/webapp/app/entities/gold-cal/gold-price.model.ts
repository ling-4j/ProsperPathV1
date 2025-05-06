export interface GoldPrice {
  stt: number;
  masp: string;
  tensp: string;
  giaban: number; // Giá bán (nghìn đồng/chỉ)
  giamua: number; // Giá mua (nghìn đồng/chỉ)
  createDate: string;
  createTime: number;
}

export interface GoldPriceResponse {
  data: GoldPrice[];
  chinhanh: string;
  updateDate: string;
  updateTime: number;
}
