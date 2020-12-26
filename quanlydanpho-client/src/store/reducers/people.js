import * as types from '../constants/types';
import initialState from '../constants/initialState';

export const people = (state = initialState.people, action) => {
    switch (action.type) {
        case types.people.GET:
            const {people} = action;
            
            return people;
        case types.people.CREATE:
            const {person} = action;
            
            return person;
        default:
            return state;
    }
};