package com.neuralrail.neuralrailapp.presentation.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.neuralrail.neuralrailapp.R
import com.neuralrail.neuralrailapp.presentation.theme.*
import com.neuralrail.neuralrailapp.presentation.viewmodels.AuthViewModel

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    var isLoginMode by remember { mutableStateOf(true) }
    val authState by authViewModel.authState.collectAsState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        GreenPrimary,
                        GreenSecondary,
                        GreenPrimaryLight
                    )
                )
            )
    ) {
        // Decorative circles
        DecorativeBackground()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            
            // Logo and App Name
            LogoSection()
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Auth Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Tab Selector
                    AuthTabSelector(
                        isLoginMode = isLoginMode,
                        onModeChange = { isLoginMode = it }
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Form Content
                    AnimatedContent(
                        targetState = isLoginMode,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(300)) + slideInHorizontally(
                                initialOffsetX = { if (targetState) -it else it }
                            ) togetherWith fadeOut(animationSpec = tween(300)) + slideOutHorizontally(
                                targetOffsetX = { if (targetState) it else -it }
                            )
                        },
                        label = "auth_content"
                    ) { loginMode ->
                        if (loginMode) {
                            LoginForm(
                                authViewModel = authViewModel,
                                onLoginSuccess = onAuthSuccess
                            )
                        } else {
                            RegisterForm(
                                authViewModel = authViewModel,
                                onRegisterSuccess = { isLoginMode = true }
                            )
                        }
                    }
                }
            }
            
            // Handle auth state changes
            LaunchedEffect(authState.isLoggedIn) {
                if (authState.isLoggedIn) {
                    onAuthSuccess()
                }
            }
            
            LaunchedEffect(authState.registrationSuccess) {
                if (authState.registrationSuccess) {
                    isLoginMode = true
                    authViewModel.clearRegistrationSuccess()
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Footer
            Text(
                stringResource(R.string.terms_privacy),
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DecorativeBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "bg_animation")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Top right circle
        Box(
            modifier = Modifier
                .size(200.dp)
                .scale(scale)
                .offset(x = 150.dp, y = (-50).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f))
        )
        // Bottom left circle
        Box(
            modifier = Modifier
                .size(300.dp)
                .scale(scale)
                .offset(x = (-100).dp, y = 500.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.08f))
        )
        // Middle circle
        Box(
            modifier = Modifier
                .size(150.dp)
                .scale(scale)
                .offset(x = 280.dp, y = 300.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.05f))
        )
    }
}

@Composable
private fun LogoSection() {
    val infiniteTransition = rememberInfiniteTransition(label = "logo_animation")
    val logoScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_scale"
    )
    
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Logo Icon
        Box(
            modifier = Modifier
                .size(100.dp)
                .scale(logoScale)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Train,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = GreenPrimary
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            stringResource(R.string.app_name),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        
        Text(
            stringResource(R.string.sustainable_travel_companion),
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.9f)
        )
    }
}

@Composable
private fun AuthTabSelector(
    isLoginMode: Boolean,
    onModeChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(GreenPrimary.copy(alpha = 0.1f))
            .padding(4.dp)
    ) {
        TabButton(
            text = stringResource(R.string.login),
            isSelected = isLoginMode,
            onClick = { onModeChange(true) },
            modifier = Modifier.weight(1f)
        )
        TabButton(
            text = stringResource(R.string.signup),
            isSelected = !isLoginMode,
            onClick = { onModeChange(false) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) GreenPrimary else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.SemiBold,
            color = if (isSelected) Color.White else GreenPrimary
        )
    }
}

@Composable
private fun LoginForm(
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current
    val authState by authViewModel.authState.collectAsState()
    
    val validEmailError = stringResource(R.string.valid_email_error)
    val passwordMinError = stringResource(R.string.password_min_error)
    
    // Show error from ViewModel
    LaunchedEffect(authState.error) {
        authState.error?.let {
            if (it.contains("password", ignoreCase = true)) {
                passwordError = it
            } else {
                emailError = it
            }
            authViewModel.clearError()
        }
    }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            stringResource(R.string.welcome_back),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = GreenPrimaryDark
        )
        Text(
            stringResource(R.string.sign_in_continue),
            fontSize = 14.sp,
            color = Color.Gray
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Email Field
        AuthTextField(
            value = email,
            onValueChange = { email = it; emailError = null },
            label = stringResource(R.string.email),
            leadingIcon = Icons.Outlined.Email,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            error = emailError
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Password Field
        AuthTextField(
            value = password,
            onValueChange = { password = it; passwordError = null },
            label = stringResource(R.string.password),
            leadingIcon = Icons.Outlined.Lock,
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            ),
            error = passwordError
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Forgot Password
        TextButton(
            onClick = { },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(stringResource(R.string.forgot_password), color = GreenPrimary, fontSize = 13.sp)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Login Button
        AuthButton(
            text = stringResource(R.string.login),
            isLoading = authState.isLoading,
            onClick = {
                var hasError = false
                if (email.isBlank() || !email.contains("@")) {
                    emailError = validEmailError
                    hasError = true
                }
                if (password.length < 6) {
                    passwordError = passwordMinError
                    hasError = true
                }
                if (!hasError) {
                    authViewModel.login(email, password)
                }
            }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Social Login
        SocialLoginSection()
    }
}

@Composable
private fun RegisterForm(
    authViewModel: AuthViewModel,
    onRegisterSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current
    val authState by authViewModel.authState.collectAsState()
    
    val nameRequiredError = stringResource(R.string.name_required_error)
    val validEmailError = stringResource(R.string.valid_email_error)
    val phoneErrorText = stringResource(R.string.phone_error)
    val passwordMinError = stringResource(R.string.password_min_error)
    val passwordsNotMatchError = stringResource(R.string.passwords_not_match)
    
    // Show error from ViewModel
    LaunchedEffect(authState.error) {
        authState.error?.let {
            emailError = it
            authViewModel.clearError()
        }
    }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            stringResource(R.string.create_account),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = GreenPrimaryDark
        )
        Text(
            stringResource(R.string.join_sustainable_movement),
            fontSize = 14.sp,
            color = Color.Gray
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Full Name Field
        AuthTextField(
            value = fullName,
            onValueChange = { fullName = it; nameError = null },
            label = stringResource(R.string.full_name),
            leadingIcon = Icons.Outlined.Person,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            error = nameError
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Email Field
        AuthTextField(
            value = email,
            onValueChange = { email = it; emailError = null },
            label = stringResource(R.string.email),
            leadingIcon = Icons.Outlined.Email,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            error = emailError
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Phone Field
        AuthTextField(
            value = phone,
            onValueChange = { phone = it; phoneError = null },
            label = stringResource(R.string.phone_number),
            leadingIcon = Icons.Outlined.Phone,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            error = phoneError
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Password Field
        AuthTextField(
            value = password,
            onValueChange = { password = it; passwordError = null },
            label = stringResource(R.string.password),
            leadingIcon = Icons.Outlined.Lock,
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = Color.Gray)
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            error = passwordError
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Confirm Password Field
        AuthTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it; confirmPasswordError = null },
            label = stringResource(R.string.confirm_password),
            leadingIcon = Icons.Outlined.Lock,
            trailingIcon = {
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = Color.Gray)
                }
            },
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            error = confirmPasswordError
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Password Requirements
        PasswordRequirements(password)
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Register Button
        AuthButton(
            text = stringResource(R.string.create_account),
            isLoading = authState.isLoading,
            onClick = {
                var hasError = false
                if (fullName.isBlank()) { nameError = nameRequiredError; hasError = true }
                if (email.isBlank() || !email.contains("@")) { emailError = validEmailError; hasError = true }
                if (phone.length < 10) { phoneError = phoneErrorText; hasError = true }
                if (password.length < 6) { passwordError = passwordMinError; hasError = true }
                if (password != confirmPassword) { confirmPasswordError = passwordsNotMatchError; hasError = true }
                if (!hasError) {
                    authViewModel.register(email, fullName, phone, password)
                }
            }
        )
    }
}

@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    error: String? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            leadingIcon = { Icon(leadingIcon, null, tint = if (error != null) ErrorRed else GreenPrimary) },
            trailingIcon = trailingIcon,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            isError = error != null,
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GreenPrimary,
                unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                focusedLabelColor = GreenPrimary,
                cursorColor = GreenPrimary
            ),
            modifier = Modifier.fillMaxWidth()
        )
        if (error != null) {
            Text(error, color = ErrorRed, fontSize = 12.sp, modifier = Modifier.padding(start = 16.dp, top = 4.dp))
        }
    }
}

@Composable
private fun AuthButton(
    text: String,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
        } else {
            Text(text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun PasswordRequirements(password: String) {
    val hasMinLength = password.length >= 6
    val hasUpperCase = password.any { it.isUpperCase() }
    val hasNumber = password.any { it.isDigit() }
    
    Column(modifier = Modifier.fillMaxWidth()) {
        RequirementItem(stringResource(R.string.at_least_6_chars), hasMinLength)
        RequirementItem(stringResource(R.string.contains_uppercase), hasUpperCase)
        RequirementItem(stringResource(R.string.contains_number), hasNumber)
    }
}

@Composable
private fun RequirementItem(text: String, isMet: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
        Icon(
            if (isMet) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            null,
            tint = if (isMet) GreenPrimary else Color.Gray.copy(alpha = 0.5f),
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(text, fontSize = 12.sp, color = if (isMet) GreenPrimary else Color.Gray)
    }
}

@Composable
private fun SocialLoginSection() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color.Gray.copy(alpha = 0.3f))
            Text("  ${stringResource(R.string.or_continue_with)}  ", color = Color.Gray, fontSize = 13.sp)
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color.Gray.copy(alpha = 0.3f))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SocialButton(icon = Icons.Default.Email, label = stringResource(R.string.google))
            SocialButton(icon = Icons.Default.Phone, label = stringResource(R.string.phone))
        }
    }
}

@Composable
private fun SocialButton(icon: ImageVector, label: String) {
    OutlinedButton(
        onClick = { },
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f)),
        modifier = Modifier.height(48.dp)
    ) {
        Icon(icon, null, tint = GreenPrimary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, color = Color.DarkGray)
    }
}
