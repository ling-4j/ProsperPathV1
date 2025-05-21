import { Directive, ElementRef, HostListener, Input, OnInit } from '@angular/core';
import { NgControl } from '@angular/forms';

@Directive({
  selector: '[jhiNumberFormat]',
})
export class NumberFormatDirective implements OnInit {
  @Input() separator = ','; // Dấu phân cách, mặc định là ',' (có thể đổi thành '.')

  constructor(
    private el: ElementRef,
    private control: NgControl,
  ) {}

  ngOnInit(): void {
    // Khi load lại trang, format lại giá trị nếu có
    const value = this.control.control?.value;
    if (value) {
      this.el.nativeElement.value = this.formatNumber(value);
    }
  }

  @HostListener('input', ['$event'])
  onInput(): void {
    const input = this.el.nativeElement as HTMLInputElement;
    const value = input.value.replace(/[^0-9]/g, '');
    if (value) {
      this.control.control?.setValue(Number(value), { emitEvent: false });
      setTimeout(() => (input.value = this.formatNumber(Number(value))));
    } else {
      this.control.control?.setValue(null, { emitEvent: false });
      setTimeout(() => (input.value = ''));
    }
  }

  @HostListener('blur', ['$event'])
  onBlur(event: Event): void {
    const input = this.el.nativeElement as HTMLInputElement;
    const value = this.control.control?.value;
    if (value) {
      input.value = this.formatNumber(value);
    }
  }

  private formatNumber(value: number): string {
    return value.toString().replace(/\B(?=(\d{3})+(?!\d))/g, this.separator);
  }
}
