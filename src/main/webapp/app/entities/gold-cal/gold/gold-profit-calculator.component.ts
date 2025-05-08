import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, NavigationEnd } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import TranslateDirective from '../../../shared/language/translate.directive';
import { GoldPriceService } from '../../gold-cal/service/gold-price.service';
import { GoldPrice } from '../../gold-cal/gold-price.model';
import { NumberFormatDirective } from 'app/shared/directive/number-format.directive';

@Component({
  selector: 'jhi-gold-profit-calculator',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateDirective, TranslateModule, NumberFormatDirective],
  templateUrl: './gold-profit-calculator.component.html',
  styleUrls: ['./gold-profit-calculator.component.scss'],
})
export default class GoldProfitCalculatorComponent implements OnInit {
  quantity = 1;
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
    this.goldPriceService.getGoldPrice('11').subscribe({
      next: (response: any) => {
        if (response?.data?.length > 0) {
          this.goldTypes = response.data;
          this.selectedType = this.goldTypes[0]?.tensp || ''; // Chọn loại vàng đầu tiên mặc định
        } else {
          this.goldTypes = [];
        }
      },
      error: (error: any) => {
        console.error('Error fetching gold prices:', error);
        this.goldTypes = [];
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
