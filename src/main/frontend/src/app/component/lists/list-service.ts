import {inject, Injectable} from '@angular/core';
import {RestClientService} from '../../service/rest.client.service';
import {Observable} from 'rxjs';
import {Group, GroupRequest, GroupType} from '../../model/group.model';
import {ShoppingListService} from '../shopping-lists/shopping-list.service';
import {ShoppingListResponse} from '../shopping-lists/shopping-list.model';

@Injectable({
  providedIn: 'root',
})
export class ListService extends RestClientService {

  private readonly apiUrl: string = '/api/lists';
  private readonly shoppingListService: ShoppingListService = inject(ShoppingListService);

  public getListsWhereMember(): Observable<Group[]> {
    return this.get<Group[]>(this.apiUrl);
  }

  public deleteList(groupId: number, type: GroupType, isOwner: boolean): Observable<void> {
    if (type === 'SHOPPING') {
      if (isOwner) {
        return this.shoppingListService.deleteShoppingList(groupId);
      } else {
        return this.shoppingListService.leaveShoppingList(groupId);
      }

    } else if (type === 'RECIPE') {

    }
    throw new Error('aaa')
  }

  public addNewList(request: GroupRequest): Observable<ShoppingListResponse> {
    if (request.type === 'SHOPPING') {
      return this.shoppingListService.createNew(request.name);
    } else if (request.type === 'RECIPE') {

    }
    throw new Error('aaa')
  }

}
