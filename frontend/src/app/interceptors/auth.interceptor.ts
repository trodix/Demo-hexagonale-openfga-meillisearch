import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  // Accéder directement au sessionStorage pour éviter la dépendance circulaire
  const authHeader = sessionStorage.getItem('auth_credentials');
  const tenantId = sessionStorage.getItem('auth_tenant_id');

  if (authHeader && tenantId && !req.url.includes('/login')) {
    const authReq = req.clone({
      setHeaders: {
        Authorization: `Basic ${authHeader}`,
        'X-Tenant-Id': tenantId
      }
    });
    return next(authReq);
  }

  return next(req);
};
