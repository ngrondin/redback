
export class ObjectResp {
    objects: object;
}

export class RbObject {
    uid: string;
    domain: string;
    name: string;
    data: Object;

    constructor(json: Object) {
        this.uid = 'a';
        this.name = 'workorder';
    }
}