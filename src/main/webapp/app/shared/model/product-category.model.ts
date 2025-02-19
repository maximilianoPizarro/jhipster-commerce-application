export interface IProductCategory {
  id?: number;
  name?: string;
  description?: string | null;
}

export class ProductCategory implements IProductCategory {
  constructor(
    public id?: number,
    public name?: string,
    public description?: string | null,
  ) {}
}
