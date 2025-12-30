import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'truncate',
  standalone: true,
})
export class TruncatePipe implements PipeTransform {
  transform(value: string | null | undefined, limit = 60): string {
    if (!value) return '';

    if (value.length <= limit) return value;

    // Cắt tạm
    let truncated = value.substring(0, limit);

    // Ưu tiên cắt tại khoảng trắng cuối
    const lastSpace = truncated.lastIndexOf(' ');
    if (lastSpace > limit * 0.6) {
      truncated = truncated.substring(0, lastSpace);
    } else {
      // Nếu không có space, thử cắt theo dấu .
      const lastDot = truncated.lastIndexOf('.');
      if (lastDot > limit * 0.6) {
        truncated = truncated.substring(0, lastDot + 1);
      }
    }

    return truncated.trim() + '...';
  }
}
