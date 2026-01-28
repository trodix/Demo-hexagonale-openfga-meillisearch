import { Component, inject, signal, computed, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { AuthService } from '../../services/auth.service';
import { ProductService } from '../../services/product.service';
import { SearchStateService } from '../../services/search-state';
import { Product } from '../../models/product.model';
import { debounceTime, distinctUntilChanged } from 'rxjs';

export interface SortOption {
  label: string;
  value: { [key: string]: string } | null;
}

@Component({
  selector: 'app-product-search',
  imports: [
    ReactiveFormsModule,
    MatToolbarModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatProgressSpinnerModule,
    MatSelectModule
  ],
  templateUrl: './product-search.component.html',
  styleUrl: './product-search.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: {
    '(window:scroll)': 'onScroll()'
  }
})
export class ProductSearchComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly productService = inject(ProductService);
  private readonly searchStateService = inject(SearchStateService);
  private readonly router = inject(Router);

  protected readonly searchControl = new FormControl('');
  protected readonly sortControl = new FormControl<{ [key: string]: string } | null>(null);
  protected readonly products = signal<Product[]>([]);
  protected readonly loading = signal(false);
  protected readonly loadingMore = signal(false);
  protected readonly hasMoreElements = signal(false);
  protected readonly currentOffset = signal(0);
  protected readonly limit = 20;

  protected readonly sortOptions: SortOption[] = [
    { label: 'Pertinence', value: null },
    { label: 'Titre (A-Z)', value: { title: 'asc' } },
    { label: 'Titre (Z-A)', value: { title: 'desc' } },
    { label: 'Prix croissant', value: { price: 'asc' } },
    { label: 'Prix décroissant', value: { price: 'desc' } },
    { label: 'Note décroissante', value: { rating: 'desc' } },
    { label: 'Stock décroissant', value: { stock: 'desc' } }
  ];

  protected readonly username = computed(() => this.authService.username());
  protected readonly tenantId = computed(() => this.authService.tenantId());
  protected readonly isAdmin = computed(() => this.authService.isAdmin());
  protected readonly hasMore = computed(() => this.hasMoreElements());
  protected readonly isEmpty = computed(() => !this.loading() && this.products().length === 0 && this.searchControl.value);

  constructor() {
    this.setupSearchListener();
    this.setupSortListener();
  }

  ngOnInit(): void {
    const savedState = this.searchStateService.getState();
    if (savedState) {
      // Restaurer l'état
      this.searchControl.setValue(savedState.query, { emitEvent: false });
      this.sortControl.setValue(savedState.sort, { emitEvent: false });
      this.products.set(savedState.products);
      this.currentOffset.set(savedState.offset);
      this.hasMoreElements.set(savedState.hasMoreElements);

      // Restaurer la position de scroll après que la vue soit rendue
      setTimeout(() => {
        window.scrollTo(0, savedState.scrollPosition);
      }, 0);
    }
  }

  private setupSearchListener(): void {
    this.searchControl.valueChanges
      .pipe(
        debounceTime(300),
        distinctUntilChanged()
      )
      .subscribe((query) => {
        if (query && query.trim()) {
          this.resetSearch();
          this.performSearch(query);
        } else {
          this.products.set([]);
          this.hasMoreElements.set(false);
          this.currentOffset.set(0);
        }
      });
  }

  private setupSortListener(): void {
    this.sortControl.valueChanges.subscribe(() => {
      const query = this.searchControl.value;
      if (query && query.trim()) {
        this.resetSearch();
        this.performSearch(query);
      }
    });
  }

  private resetSearch(): void {
    this.products.set([]);
    this.currentOffset.set(0);
    this.hasMoreElements.set(false);
    this.searchStateService.clearState();
  }

  private performSearch(query: string, isLoadMore: boolean = false): void {
    const sort = this.sortControl.value || undefined;

    if (isLoadMore) {
      this.loadingMore.set(true);
    } else {
      this.loading.set(true);
    }

    const offset = this.currentOffset();

    this.productService.searchProducts(query, offset, this.limit, sort || undefined).subscribe({
      next: (response) => {

        if (isLoadMore) {
          this.products.update(current => [...current, ...response.entries]);
        } else {
          this.products.set(response.entries);
        }

        this.hasMoreElements.set(response.pageable.hasMoreElements);
        this.currentOffset.update(current => current + response.entries.length);
        this.loading.set(false);
        this.loadingMore.set(false);
      },
      error: (error) => {
        console.error('Error searching products:', error);
        this.loading.set(false);
        this.loadingMore.set(false);
      }
    });
  }

  protected onScroll(): void {
    const scrollPosition = window.innerHeight + window.scrollY;
    const threshold = document.documentElement.scrollHeight - 200;

    if (
      scrollPosition >= threshold &&
      !this.loading() &&
      !this.loadingMore() &&
      this.hasMore() &&
      this.searchControl.value
    ) {
      this.performSearch(this.searchControl.value, true);
    }
  }

  protected logout(): void {
    this.searchStateService.clearState();
    this.authService.logout();
  }

  protected goToAdmin(): void {
    this.router.navigate(['/admin/permissions']);
  }

  protected trackByProductId(index: number, product: Product): number {
    return product.id;
  }

  protected getTitle(product: Product): string {
    return product._formatted?.title || product.title;
  }

  protected getDescription(product: Product): string {
    return product._formatted?.description || product.description;
  }

  protected getCategory(product: Product): string | undefined {
    return product._formatted?.category || product.category;
  }

  protected getBrand(product: Product): string | undefined {
    return product._formatted?.brand || product.brand;
  }

  protected getDiscountedPrice(product: Product): number | null {
    if (product.discountPercentage) {
      return product.price * (1 - product.discountPercentage / 100);
    }
    return null;
  }

  protected viewProduct(id: number): void {
    // Sauvegarder l'état actuel avant de naviguer
    const query = this.searchControl.value;
    if (query) {
      this.searchStateService.saveState({
        query,
        sort: this.sortControl.value,
        products: this.products(),
        offset: this.currentOffset(),
        hasMoreElements: this.hasMoreElements(),
        scrollPosition: window.scrollY
      });
    }

    this.router.navigate(['/products', id]);
  }
}
