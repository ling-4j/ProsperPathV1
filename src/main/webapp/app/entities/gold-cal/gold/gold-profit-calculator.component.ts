import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, NavigationEnd } from '@angular/router';

@Component({
    selector: 'jhi-gold-profit-calculator',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './gold-profit-calculator.component.html',
    styleUrls: ['./gold-profit-calculator.component.scss'],
})
export default class GoldProfitCalculatorComponent implements OnInit {
    quantity: number = 1;
    unit = 'chỉ';
    selectedType = '';
    purchasePrice: number | null = null;

    goldTypes = [
        { name: 'Vàng miếng SJC', buy: 11750000, sell: 12000000 },
        { name: 'Nhẫn 999.9', buy: 10750000, sell: 11000000 },
        { name: 'Vàng Ta (990)', buy: 10600000, sell: 10950000 },
        { name: 'Vàng 18k (750)', buy: 7505000, sell: 8100000 },
        { name: 'Vàng trắng Au750', buy: 7505000, sell: 8100000 },
    ];

    currentDate = new Date();
    result: number | null = null;
    isNoteVisible: boolean = true;
    isGoldCalRoute: boolean = false;

    constructor(private router: Router) {}

    ngOnInit(): void {
        // Kiểm tra route ban đầu
        this.isGoldCalRoute = this.router.url.includes('/gold-cal');

        // Theo dõi sự kiện điều hướng để cập nhật isGoldCalRoute
        this.router.events.subscribe(event => {
            if (event instanceof NavigationEnd) {
                this.isGoldCalRoute = event.urlAfterRedirects.includes('/gold-cal');
            }
        });
    }

    calculate(): void {
        const type = this.goldTypes.find(t => t.name === this.selectedType);
        if (!type || this.purchasePrice === null || this.quantity < 1 || isNaN(this.quantity)) {
            this.result = null;
            this.isNoteVisible = true;
            return;
        }

        const profitPerUnit = type.sell - this.purchasePrice;
        this.result = profitPerUnit * this.quantity;
        this.isNoteVisible = false;
    }

    onQuantityChange(): void {
        if (this.quantity < 1 || isNaN(this.quantity)) {
            this.quantity = 1;
        }
    }
}