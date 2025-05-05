import { Component, AfterViewInit } from '@angular/core';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'jhi-carousel',
  standalone: true,
  templateUrl: './carousel.component.html',
  styleUrls: ['./carousel.component.scss'],
  imports: [RouterModule]
})
export default class CarouselComponent implements AfterViewInit {
  ngAfterViewInit(): void {
    console.log('Carousel initialized');
    const carouselElement = document.getElementById('prosperPathCarousel');
    if (carouselElement && typeof (window as any).bootstrap !== 'undefined') {
      console.log('Bootstrap JS loaded, initializing carousel');
      new (window as any).bootstrap.Carousel(carouselElement);
    } else {
      console.error('Bootstrap JS not loaded or carousel element not found');
    }
  }
}