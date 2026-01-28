import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { Product, ProductSearchResponse } from '../models/product.model';

export interface ProductQueryRequest {
  params: {
    searchTerms: string;
    attributesToHighlight?: string[];
    highlightPreTag?: string;
    highlightPostTag?: string;
    sort?: { [key: string]: string };
  };
  paging: {
    offset: number;
    limit: number;
  };
}

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/products/search';

  searchProducts(query: string, offset: number = 0, limit: number = 20, sort?: { [key: string]: string }): Observable<ProductSearchResponse> {
    const body: ProductQueryRequest = {
      params: {
        searchTerms: query,
        attributesToHighlight: ['title', 'description', 'category', 'brand'],
        highlightPreTag: '<mark class="search-highlight">',
        highlightPostTag: '</mark>'
      },
      paging: {
        offset,
        limit
      }
    };

    if (sort && Object.keys(sort).length > 0) {
      body.params.sort = sort;
    }

    return this.http.post<ProductSearchResponse>(this.apiUrl, body);
  }

  getProduct(id: number): Observable<Product> {
    return this.http.get<Product>(`/api/products/${id}`);
  }
}
