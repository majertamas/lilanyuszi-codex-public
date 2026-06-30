import {Group, GroupNameRequest} from '../model/group.model';
import {Observable} from 'rxjs';

export interface ModifyGroupName {
  modifyGroupName(request: GroupNameRequest): Observable<Group>;
}
