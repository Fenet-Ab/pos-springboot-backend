package com.pos.app.service;

import com.pos.app.dto.LoginRequestDTO;
import com.pos.app.dto.RegisterRequestDTO;

public interface AuthService {
    String registerManager(RegisterRequestDTO request);
    String registerCashier(RegisterRequestDTO request);
    String login(LoginRequestDTO request);


}
