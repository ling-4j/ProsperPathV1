import { Directive, ElementRef, HostListener, Input } from '@angular/core';
import { NgControl } from '@angular/forms';

@Directive({
  selector: '[appNumberFormat]',
})
export class NumberFormatDirective {
  @Input() separator: string = ','; // Dấu phân cách, mặc định là ',' (có thể đổi thành '.')

  constructor(private el: ElementRef, private control: NgControl) {}

  @HostListener('input', ['$event'])
  onInput(event: Event): void {
    const input = this.el.nativeElement as HTMLInputElement;
    let value = input.value.replace(/[^0-9]/g, ''); // Chỉ giữ lại số

    if (value) {
      // Định dạng số với dấu phân cách
      const formattedValue = this.formatNumber(parseInt(value, 10));
      input.value = formattedValue;

      // Lưu giá trị không có dấu phân cách vào form
      this.control.control?.setValue(parseInt(value, 10), { emitEvent: false });
    } else {
      this.control.control?.setValue(null, { emitEvent: false });
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