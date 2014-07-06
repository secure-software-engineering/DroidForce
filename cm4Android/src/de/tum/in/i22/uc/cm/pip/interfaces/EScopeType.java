package de.tum.in.i22.uc.cm.pip.interfaces;


// TODO Ugly. Generic type ScopeType should not contain layer/application specific parts.
// TODO: Diasgree. This is the list of all the kind of scopes we can have. this list is "layer-pairs"-wise specific
public enum EScopeType {
	UNKNOWN, SAVE_FILE, LOAD_FILE, COPY_CLIPBOARD, PASTE_CLIPBOARD, SEND_SOCKET, READ_SOCKET, SEND_EMAIL, GET_EMAIL, JBC_GENERIC_IN, JBC_GENERIC_OUT
}
