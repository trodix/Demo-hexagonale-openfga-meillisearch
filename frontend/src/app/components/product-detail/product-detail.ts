import { Component, inject, signal, computed, input, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatToolbarModule } from '@angular/material/toolbar';
import { ProductService } from '../../services/product.service';
import { Product } from '../../models/product.model';

@Component({
  selector: 'app-product-detail',
  imports: [
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatProgressSpinnerModule,
    MatToolbarModule
  ],
  templateUrl: './product-detail.html',
  styleUrl: './product-detail.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProductDetail implements OnInit {
  private readonly productService = inject(ProductService);
  private readonly router = inject(Router);

  readonly id = input.required<string>();
  protected readonly product = signal<Product | null>(null);
  protected readonly loading = signal(true);
  protected readonly error = signal(false);

  protected readonly discountedPrice = computed(() => {
    const p = this.product();
    if (p && p.discountPercentage) {
      return p.price * (1 - p.discountPercentage / 100);
    }
    return null;
  });

  ngOnInit(): void {
    const productId = Number(this.id());
    if (productId) {
      this.loadProduct(productId);
    }
  }

  private loadProduct(id: number): void {
    this.loading.set(true);
    this.error.set(false);

    this.productService.getProduct(id).subscribe({
      next: (product) => {
        this.product.set(product);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error loading product:', err);
        this.error.set(true);
        this.loading.set(false);
      }
    });
  }

  protected goBack(): void {
    this.router.navigate(['/search']);
  }
}
