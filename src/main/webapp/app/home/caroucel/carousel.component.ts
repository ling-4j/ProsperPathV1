import { Component, AfterViewInit } from '@angular/core';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'jhi-carousel',
  standalone: true,
  templateUrl: './carousel.component.html',
  styleUrls: ['./carousel.component.scss'],
  imports: [RouterModule],
})
export default class CarouselComponent implements AfterViewInit {
  ngAfterViewInit(): void {
    const carouselElement = document.getElementById('prosperPathCarousel');
    if (carouselElement && typeof (window as any).bootstrap !== 'undefined') {
      (window as any).bootstrap.Carousel(carouselElement);
    }
  }
}
