import { Pipe, PipeTransform } from '@angular/core';
import { environment } from '../../../environments/environment';

@Pipe({
  name: 'imageUrl',
  standalone: true
})
export class ImageUrlPipe implements PipeTransform {
  transform(url: string | undefined): string {
    if (!url) return 'assets/placeholder-book.png';
    
    // If it's already an absolute URL (http/https), return it as is
    if (url.startsWith('http://') || url.startsWith('https://')) {
      return url;
    }
    
    // If it's a relative path, prefix it with the backend API URL
    const baseUrl = environment.apiUrl;
    const cleanPath = url.startsWith('/') ? url : `/${url}`;
    
    // Some backend setups might already include /api/books in the path
    // We assume the URL returned is relative to the base API domain
    return `${baseUrl}${cleanPath}`;
  }
}
