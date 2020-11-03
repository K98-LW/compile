#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define CMP_ERROR -10

char str[1005];
int cmp[136][136], top = -1;
char stack[1005];

int is_vt(char c){
    return c == '+' || c == '(' || c == ')' || c == 'i' || c == '*' || c == '#';
}

int is_vn(char c){
    return c == 'N' || c == 'F' || c == 'T' || c == 'E';
}

void push_stack(char c){
    stack[++top] = c;
    if(is_vt(c) && c != '#')
        printf("I%c\n", c);
}

int pop_stack(){
    if(top < 0)
        return -1;
    return stack[top--];
}

int top_stack(){
    if(top < 0)
        return -1;
    return stack[top];
}

void init(){
    int i, j;
    push_stack('#');

    for(i=0; i<136; i++)
        for(j=0; j<136; j++)
            cmp[i][j] = CMP_ERROR;
    
    // +
    cmp['+']['+'] = 1;
    cmp['+']['*'] = -1;
    cmp['+']['i'] = -1;
    cmp['+']['('] = -1;
    cmp['+'][')'] = 1;
    cmp['+']['#'] = 1;

    // *
    cmp['*']['+'] = 1;
    cmp['*']['*'] = 1;
    cmp['*']['i'] = -1;
    cmp['*']['('] = -1;
    cmp['*'][')'] = 1;
    cmp['*']['#'] = 1;

    // i
    cmp['i']['+'] = 1;
    cmp['i']['*'] = 1;
    cmp['i'][')'] = 1;
    cmp['i']['#'] = 1;

    // (
    cmp['(']['+'] = -1;
    cmp['(']['*'] = -1;
    cmp['(']['i'] = -1;
    cmp['(']['('] = -1;
    cmp['('][')'] = 0;

    // )
    cmp[')']['+'] = 1;
    cmp[')']['*'] = 1;
    cmp[')'][')'] = 1;
    cmp[')']['#'] = 1;

    // #
    cmp['#']['+'] = -1;
    cmp['#']['*'] = -1;
    cmp['#']['i'] = -1;
    cmp['#']['('] = -1;
}

int is_legal_input(char c){
    if(is_vt(c) && c != '#')
        return 1;
    return 0;
}

int m_statute(){
    int c = pop_stack();
    if(c == -1) return 0;

    if(is_vn(c)){
        c = pop_stack();
        if(c == -1) return 0;

        if(c == '+' || c == '*'){
            c = pop_stack();
            if(c == -1) return 0;

            if(is_vn(c)) return 1;
            else return 0;
        }
        else return 0;
    }
    else if(is_vt(c)){
        if(c == 'i') return 1;
        else if(c == ')'){
            c = pop_stack();
            if(c == -1) return 0;

            if(is_vn(c)){
                c = pop_stack();
                if(c == -1) return 0;

                if(c == '(') return 1;
                else return 0;
            }
            else return 0;
        }
        else return 0;
    }
    else return 0;
}

int statute(){
    int res = m_statute();
    if(res == 1){
        push_stack('N');
        printf("R\n");
    }
    else
        printf("RE\n");
    return res;
}

int last_vt(){
    int c = top_stack();
    if(is_vt(c) || c == -1){
        // printf("return c:%c\n", c);
        return c;
    }
    else{
        if(top < 1) return -1;
        return stack[top-1];
    }
}

void read_file(char *path){
    FILE *fp = fopen(path, "r");
    if(fp == NULL){
        printf("can not open the file.\n");
        exit(-1);
    }
    fscanf(fp, "%s", str);
    fclose(fp);
}

int is_end(){
    if(top != 1)
        return 0;
    return stack[0] == '#' && is_vn(stack[1]);
}

int main(int argc, char *argv[]){
    init();
    read_file(argv[1]);

    int len = strlen(str);
    str[len++] = '#';
    str[len] = 0;

    int i = 0;

    // printf("top:%d, stack[top]:%c\n", top, stack[top]);
    while(i < len){
        if(i == len-1){
            while(!is_end()){
                int res = statute();
                if(res == 0) return 0;
            }
            return 0;
        }
        else{
            if(!is_legal_input(str[i])){
                printf("E\n");
                return 0;
            }
            else{
                // printf("f top:%d, stack[top]:%c\n", top, stack[top]);
                int c1 = last_vt();
                if(c1 == 0){
                    printf("Not exist error.\n");
                    return -1;
                }

                int c2 = str[i];
                int res = cmp[c1][c2];
                // printf("cmp:%d c1'%c' c2'%c'\n", res, c1, c2);
                if(res == CMP_ERROR){
                    printf("E\n");
                    return 0;
                }
                else if(res > 0){
                    int statute_res = statute();
                    if(!statute_res) return 0;
                }
                else{
                    push_stack(c2);
                    i++;
                }
            }
        }
    }
}
