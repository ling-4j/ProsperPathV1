import { Component } from '@angular/core';
import { CommonModule } from '@angular/common'; // ✅ Thêm cái này
import { FormsModule } from '@angular/forms';
import { AiService } from './ai.service';

@Component({
  standalone: true, // ✅ Quan trọng!
  selector: 'app-ai',
  templateUrl: './ai.component.html',
  imports: [
    CommonModule,  // ✅ Bổ sung
    FormsModule,
  ]
})
export class AiComponent {
  userInput = '';
  response = '';
  loading = false;

  constructor(private aiService: AiService) {}

  askGPT() {
    this.loading = true;
    this.aiService.ask(this.userInput).subscribe(res => {
      this.response = res;
      this.loading = false;
    });
  }
}
