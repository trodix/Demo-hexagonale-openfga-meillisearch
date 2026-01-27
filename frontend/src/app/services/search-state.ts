import { Injectable, signal } from '@angular/core';
import { Product } from '../models/product.model';

export interface SearchState {
  query: string;
  sort: { [key: string]: string } | null;
  products: Product[];
  offset: number;
  hasMoreElements: boolean;
  scrollPosition: number;
}

@Injectable({
  providedIn: 'root'
})
export class SearchStateService {
  private readonly state = signal<SearchState | null>(null);

  saveState(state: SearchState): void {
    this.state.set(state);
  }

  getState(): SearchState | null {
    return this.state();
  }

  clearState(): void {
    this.state.set(null);
  }
}
