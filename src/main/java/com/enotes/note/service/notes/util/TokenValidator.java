package com.enotes.note.service.notes.util;

import com.enotes.note.service.authentication.TokenInfo;

public interface TokenValidator {
  TokenInfo extractTokenInfo(String authToken);
}
