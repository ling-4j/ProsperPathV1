import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'truncate',
  standalone: true,
})
export class TruncatePipe implements PipeTransform {
  transform(value: string, wordLimit: number = 10): string {
    if (!value) {
      return '';
    }
    const words = value.split(/\s+/); // Tách chuỗi thành mảng các từ
    if (words.length <= wordLimit) {
      return value;
    }
    return words.slice(0, wordLimit).join(' ') + '...'; // Cắt và thêm dấu ...
  }
}