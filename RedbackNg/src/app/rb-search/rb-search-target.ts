
export interface RbSearchTarget {
    objectname: string;

    filterSort(event: any) : boolean;

    getBaseSearchFilter() : any;    
}