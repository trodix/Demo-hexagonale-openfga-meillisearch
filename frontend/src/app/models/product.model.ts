export interface Product {
  id: number;
  title: string;
  description: string;
  price: number;
  category?: string;
  thumbnail?: string;
  images?: string[];
  brand?: string;
  stock?: number;
  rating?: number;
  discountPercentage?: number;
  tags?: string[];
  _formatted?: {
    title?: string;
    description?: string;
    category?: string;
    brand?: string;
    [key: string]: any;
  };
}

export interface Pageable {
  page: number;
  pageSize: number;
  hasMoreElements: boolean;
}

export interface ProductSearchResponse {
  pageable: Pageable;
  entries: Product[];
}
