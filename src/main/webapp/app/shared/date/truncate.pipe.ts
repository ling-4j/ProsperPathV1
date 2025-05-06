import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'truncate',
})
export class TruncatePipe implements PipeTransform {
  transform(value: string | null | undefined, wordLimit = 10): string {
    if (!value) {
      // Kiểm tra null hoặc undefined
      return '';
    }
    const words = value.split(/\s+/);
    if (words.length <= wordLimit) {
      return value;
    }
    return words.slice(0, wordLimit).join(' ') + '...';
  }
}
