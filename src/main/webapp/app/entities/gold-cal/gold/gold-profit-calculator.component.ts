import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, NavigationEnd } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { GoldPriceService } from '../../gold-cal/service/gold-price.service';
import { GoldPrice } from '../../gold-cal/gold-price.model';

@Component({
  selector: 'jhi-gold-profit-calculator',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule],
  templateUrl: './gold-profit-calculator.component.html',
  styleUrls: ['./gold-profit-calculator.component.scss'],
})
export default class GoldProfitCalculatorComponent implements OnInit {
  quantity = 1;
  unit = 'chỉ';
  selectedType = '';
  purchasePrice: number | null = null;
  goldTypes: GoldPrice[] = [];
  currentDate = new Date();
  result: number | null = null;
  isNoteVisible = true;
  isGoldCalRoute = false;

  constructor(
    private router: Router,
    private goldPriceService: GoldPriceService,
  ) {}

  ngOnInit(): void {
    this.isGoldCalRoute = this.router.url.includes('/gold-cal');
    this.router.events.subscribe(event => {
      if (event instanceof NavigationEnd) {
        this.isGoldCalRoute = event.urlAfterRedirects.includes('/gold-cal');
      }
    });

    // Fetch dữ liệu từ API
    this.goldPriceService.getGoldPrices().subscribe({
      next: data => {
        this.goldTypes = data;
        if (this.goldTypes.length > 0) {
          this.selectedType = this.goldTypes[0].tensp; // Chọn loại vàng đầu tiên mặc định
        }
      },
      error: error => {
        console.error('Error fetching gold prices:', error);
        // Dữ liệu fallback nếu API lỗi
        this.goldTypes = [
          {
            stt: 1,
            masp: 'SJC',
            tensp: 'Vàng miếng SJC',
            giaban: 12000,
            giamua: 11750,
            createDate: '05/05/2025',
            createTime: 1746433920000,
          },
          {
            stt: 2,
            masp: 'N24K',
            tensp: 'Nhẫn Trơn PNJ 999.9',
            giaban: 11540,
            giamua: 11250,
            createDate: '05/05/2025',
            createTime: 1746433920000,
          },
        ];
        this.selectedType = this.goldTypes[0].tensp;
      },
    });
  }

  calculate(): void {
    const type = this.goldTypes.find(t => t.tensp === this.selectedType);
    if (!type || this.purchasePrice === null || this.quantity < 1 || isNaN(this.quantity)) {
      this.result = null;
      this.isNoteVisible = true;
      return;
    }

    // API trả về giá theo nghìn đồng/chỉ, cần nhân với 1000 để tính đúng
    const sellPrice = type.giaban * 1000; // Giá bán (đ/chỉ)
    const profitPerUnit = sellPrice - this.purchasePrice;
    this.result = profitPerUnit * this.quantity;
    this.isNoteVisible = false;
  }

  onQuantityChange(): void {
    if (this.quantity < 1 || isNaN(this.quantity)) {
      this.quantity = 1;
    }
  }
}
