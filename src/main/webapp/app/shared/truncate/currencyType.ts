import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'currencyType',
  standalone: true,
})
export class CurrencyTypePipe implements PipeTransform {
  transform(value: number | null | undefined): string {
    if (value == null) return '';
    const absValue = Math.abs(value);
    return value.toLocaleString('vi-VN') + ' VND';
  }
}
