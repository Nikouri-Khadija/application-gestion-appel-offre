import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'filter'
})
export class FilterPipe implements PipeTransform {
  transform(items: any[], search: string, field: string): any[] {
    if (!items || !search || !field) return items;

    const fieldParts = field.split('.');

    return items.filter(item => {
      let value = item;
      for (const part of fieldParts) {
        if (value && value[part] !== undefined) {
          value = value[part];
        } else {
          return false;
        }
      }
      return value.toString().toLowerCase().includes(search.toLowerCase());
    });
  }
}
