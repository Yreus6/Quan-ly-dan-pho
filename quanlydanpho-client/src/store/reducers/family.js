import * as types from '../constants/types';
import initialState from '../constants/initialState';

export const family = (state = initialState.family, action) => {
    switch (action.type) {
        case types.family.GET:
            return action.family;
        case types.family.ADD_PERSON:
            return action.family;
        default:
            return state;
    }
};
