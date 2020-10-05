#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define WORD_MAX_SIZE 1005

const int UNKNOWN   = 0;
const int BEGINSY   = 1;
const int ENDSY     = 2;
const int FORSY     = 3;
const int DOSY      = 4;
const int IFSY      = 5;
const int THENSY    = 6;
const int ELSESY    = 7;
const int IDSY      = 8;
const int INTSY     = 9;
const int COLONSY   = 10;
const int PLUSSY    = 11;
const int STARSY    = 12;
const int COMSY     = 13;
const int LPARSY    = 14;
const int RPARSY    = 15;
const int ASSIGNSY  = 16;

FILE *fp;
int CHAR, TOKEN_LENGTH, RESWR_SIZE = 7;
char TOKEN[WORD_MAX_SIZE];
const char *reserved_words[] = {
    "BEGIN",
    "END",
    "FOR",
    "DO",
    "IF",
    "THEN",
    "ELSE",
};

const int reserved_id[] = {1, 2, 3, 4, 5, 6, 7};

struct word{
    int code;
    union{
        char str[WORD_MAX_SIZE];
        int num;
    } value;
} analyze_result[100005];
int analyze_length;

char *path;

void print_result(){
    int i;
    char *reserved_name[] = {
        "Unknown",
        "Begin",
        "End",
        "For",
        "Do",
        "If",
        "Then",
        "Else",
        "Ident",
        "Int",
        "Colon",
        "Plus",
        "Star",
        "Comma",
        "LParenthesis",
        "RParenthesis",
        "Assign",
    };
    for(i=0; i<analyze_length; i++){
        printf("%s", reserved_name[analyze_result[i].code]);
        if(analyze_result[i].code == IDSY){
            printf("(%s)\n", analyze_result[i].value.str);
        }
        else if(analyze_result[i].code == INTSY){
            printf("(%d)\n", analyze_result[i].value.num);
        }
        else{
            printf("\n");
        }
    }
}

void ERROR(char error_msg[]){
    // puts(error_msg);
    if(fp != NULL){
        fclose(fp);
    }
    print_result();
    exit(0);
}

void GETCHAR(){
    if(fp == NULL){
        fp = fopen(path, "r");
        if(fp == NULL){
            ERROR("can not find file code.");
            return;
        }
    }
    CHAR = fgetc(fp);
    if(CHAR == NULL){
        CHAR = EOF;
    }
    return;
}

void GETNBC(){
    while(1){
        GETCHAR();
        if(CHAR == '\n' || CHAR == '\r' || CHAR == ' ' || CHAR == '\t'){
            continue;
        }
        else{
            return;
        }
    }
}

void CLEAR_TOKEN(){
    memset(TOKEN, WORD_MAX_SIZE, 1);
    TOKEN_LENGTH = 0;
}

void CAT(){
    if(TOKEN_LENGTH == WORD_MAX_SIZE){
        ERROR("TOKEN is not big enough.");
        return;
    }
    TOKEN[TOKEN_LENGTH++] = CHAR;
}

int ISLETTER(){
    return (CHAR>='A'&&CHAR<='Z') || (CHAR>='a'&&CHAR<='z');
}

int ISDIGIT(){
    return CHAR >= '0' && CHAR <= '9';
}

void UNGETCH(){
    fseek(fp, -1, SEEK_CUR);
}

// 不是 0 保留字, 0 标识符
int RESERVE(){
    int i;
    TOKEN[TOKEN_LENGTH] = 0;
    for(i=0; i<RESWR_SIZE; i++){
        if(strcmp(TOKEN, reserved_words[i]) == 0){
            return reserved_id[i];
        }
    }
    return 0;
}

int ATOI(){
    int i;
    long long num = 0;
    for(i=0; i<TOKEN_LENGTH; i++){
        if(!(TOKEN[i] >= '0' && TOKEN[i] <= '9')){
            ERROR("TOKEN is not a number.");
            return -1;
        }
        num = num * 10 + (TOKEN[i] - '0');
        if(num > __INT_MAX__){
            ERROR("TOKEN is bigger than INT_MAX.");
            return -1;
        }
    }
    return (int)num;
}

struct word MAKE_WORD(int code, char str[]){
    struct word w;
    memset(w.value.str, WORD_MAX_SIZE, 1);
    w.code = code;
    if(code == INTSY){
        w.value.num = ATOI(str);
    }
    else{
        strncpy(w.value.str, str, WORD_MAX_SIZE);
    }
    return w;
}

void END(){
    if(fp != NULL){
        fclose(fp);
    }
    print_result();
}

int main(int argc, char *argv[]){
    path = argv[1];
    while(1){
        CLEAR_TOKEN();
        GETNBC();
        if(CHAR == EOF){
            END();
            return;
        }

        if(ISLETTER()){
            do{
                CAT();
                GETCHAR();
            } while(ISLETTER() || ISDIGIT());
            int flag = RESERVE();
            if(flag){
                analyze_result[analyze_length++] = MAKE_WORD(flag, "-");
            }
            else{
                analyze_result[analyze_length++] = MAKE_WORD(IDSY, TOKEN);
            }
        }
        else if(ISDIGIT()){
            do{
                CAT();
                GETCHAR();
            } while(ISDIGIT());
            UNGETCH();
            analyze_result[analyze_length++] = MAKE_WORD(INTSY, TOKEN);
        }
        else if(CHAR == '+'){
            analyze_result[analyze_length++] = MAKE_WORD(PLUSSY, "-");
        }
        else if(CHAR == '*'){
            analyze_result[analyze_length++] = MAKE_WORD(STARSY, "-");
        }
        else if(CHAR == ','){
            analyze_result[analyze_length++] = MAKE_WORD(COMSY, "-");
        }
        else if(CHAR == '('){
            analyze_result[analyze_length++] = MAKE_WORD(LPARSY, "-");
        }
        else if(CHAR == ')'){
            analyze_result[analyze_length++] = MAKE_WORD(RPARSY, "-");
        }
        else if(CHAR == ':'){
            GETCHAR();
            if(CHAR == '=')
                analyze_result[analyze_length++] = MAKE_WORD(ASSIGNSY, "-");
            UNGETCH();
            analyze_result[analyze_length++] = MAKE_WORD(COLONSY, "-");
        }
        else{
            analyze_result[analyze_length++] = MAKE_WORD(UNKNOWN, "-");
            // ERROR("illegal input.");
        }
    }
}
