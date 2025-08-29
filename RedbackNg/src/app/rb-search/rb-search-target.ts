
export interface RbSearchTarget {

    filterSort(event: any) : boolean;

    getId() : string;

    getSearchTargetType(): string;

    getObjectName(): string;

    getUserFilter() : any;

    getUserSort() : any;

    getBaseFilter() : any;    
}