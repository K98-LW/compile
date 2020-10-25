#include "tokenizer/tokenizer.h"

#include <cctype>
#include <sstream>

namespace miniplc0
{

std::pair<std::optional<Token>,std::optional<CompilationError>>Tokenizer::NextToken()// ���ĺ�����������һ�� token
{
	if(!_initialized)
	{
		readAll();
	}
	if(_rdr.bad())
	{
		return std::make_pair(std::optional<Token>(),std::make_optional<CompilationError>(0, 0, ErrorCode::ErrStreamError));
	}
	if(isEOF())
	{
		return std::make_pair(std::optional<Token>(),std::make_optional<CompilationError>(0, 0, ErrorCode::ErrEOF));
	}
	auto p=nextToken();
	if(p.second.has_value())
	{
		return std::make_pair(p.first, p.second);
	}
	auto err=checkToken(p.first.value());
	if(err.has_value())
	{
		return std::make_pair(p.first, err.value());
	}
	return std::make_pair(p.first, std::optional<CompilationError>());
}

std::pair<std::vector<Token>, std::optional<CompilationError>> Tokenizer::AllTokens()// һ�η������� token
{
	std::vector<Token> result;
	while(true)
	{
		auto p=NextToken();
		if(p.second.has_value())
		{
			if (p.second.value().GetCode() == ErrorCode::ErrEOF)
			{
				return std::make_pair(result, std::optional<CompilationError>());
			}
			else
			{
				return std::make_pair(std::vector<Token>(), p.second);
			}
		}
		result.emplace_back(p.first.value());
	}
}

// ע�⣺����ķ���ֵ�� Token �� CompilationError ֻ�ܷ���һ��������ͬʱ���ء�
std::pair<std::optional<Token>,std::optional<CompilationError>> Tokenizer::nextToken()// ������һ�� token���� NextToken ʵ��ʵ�ֲ���
{
	std::stringstream ss;// ���ڴ洢�Ѿ���������ɵ�ǰtoken�ַ�
	std::pair<std::optional<Token>,std::optional<CompilationError>> result;// ����token�Ľ������Ϊ�˺����ķ���ֵ
	std::pair<int64_t,int64_t> pos;// <�кţ��к�>����ʾ��ǰtoken�ĵ�һ���ַ���Դ�����е�λ��
	DFAState current_state=DFAState::INITIAL_STATE;// ��¼��ǰ�Զ�����״̬������˺���ʱ�ǳ�ʼ״̬
// ����һ����ѭ����������������
// ÿһ��ִ��while�ڵĴ��룬�����ܵ���״̬�ı��
	while(true)
	{
// ��һ���ַ�����ע��auto�Ƶ��ó���������std::optional<char>
// ������ʵ������д��
// 1. ÿ��ѭ��ǰ��������һ�� char
// 2. ֻ���ڿ��ܻ�ת�Ƶ�״̬����һ�� char
// ��Ϊ����ʵ���� unread��Ϊ��ʡ������ѡ���һ��
		auto current_char=nextChar();
		switch(current_state)// ��Ե�ǰ��״̬���в�ͬ�Ĳ���
		{
// ��� case ���Ǹ����˺����߼������Ǻ���� case �����հᡣ
			case INITIAL_STATE:// ��ʼ״̬
			{
				if(!current_char.has_value())// �Ѿ��������ļ�β
				{
					return std::make_pair(std::optional<Token>(),std::make_optional<CompilationError>(0,0,ErrEOF));// ����һ���յ�token���ͱ������ErrEOF���������ļ�β
				}
				auto ch=current_char.value();// ��ȡ�������ַ���ֵ��ע��auto�Ƶ�����������char
				auto invalid=false;// ����Ƿ�����˲��Ϸ����ַ�����ʼ��Ϊ��
// ʹ�����Լ���װ���ж��ַ����͵ĺ����������� tokenizer/utils.hpp
// see https://en.cppreference.com/w/cpp/string/byte/isblank
				if(miniplc0::isspace(ch))  // �������ַ��ǿհ��ַ����ո񡢻��С��Ʊ���ȣ�
				{
					current_state=DFAState::INITIAL_STATE;  // ������ǰ״̬Ϊ��ʼ״̬���˴�ֱ��breakҲ�ǿ��Ե�
				}
				else if(!miniplc0::isprint(ch))//���ƴ�����˸�
				{
					invalid=true;
				}
				else if(miniplc0::isdigit(ch))  // �������ַ�������
				{
					current_state=DFAState::UNSIGNED_INTEGER_STATE;  // �л����޷���������״̬
				}
				else if(miniplc0::isalpha(ch))  // �������ַ���Ӣ����ĸ
				{
					current_state=DFAState::IDENTIFIER_STATE;  // �л�����ʶ����״̬
				}
				else
				{
					switch(ch)
					{
						case '=':  // ����������ַ���`=`�����л������ںŵ�״̬
							current_state=DFAState::EQUAL_SIGN_STATE;
						break;
						case '-':
							current_state=DFAState::MINUS_SIGN_STATE;// ����գ��л������ŵ�״̬
						break;
						case '+':
							current_state=DFAState::PLUS_SIGN_STATE;// ����գ��л����Ӻŵ�״̬
						break;
						case '*':
							current_state=DFAState::MULTIPLICATION_SIGN_STATE;// ����գ��л�״̬
						break;
						case '/':
							current_state=DFAState::DIVISION_SIGN_STATE;// ����գ��л�״̬
						break;
///// ����գ����������Ŀɽ����ַ����л�����Ӧ��״̬
						case ';':
							current_state=DFAState::SEMICOLON_STATE;
						break;
						case '(':
							current_state=DFAState::LEFTBRACKET_STATE;
						break;
						case ')':
							current_state=DFAState::RIGHTBRACKET_STATE;
						break;
						default:// �����ܵ��ַ����µĲ��Ϸ���״̬
							invalid=true;
						break;
					}
				}
				if(current_state != DFAState::INITIAL_STATE)// ����������ַ�������״̬��ת�ƣ�˵������һ��token�ĵ�һ���ַ�
				{
					pos = previousPos();  // ��¼���ַ��ĵ�λ��Ϊtoken�Ŀ�ʼλ��
				}
				if(invalid)// �����˲��Ϸ����ַ�
				{
					unreadLast();// ��������ַ�
					return std::make_pair(std::optional<Token>(),std::make_optional<CompilationError>(pos, ErrorCode::ErrInvalidInput));// ���ر�����󣺷Ƿ�������
				}
				// ����������ַ�������״̬��ת�ƣ�˵������һ��token�ĵ�һ���ַ�
				if(current_state != DFAState::INITIAL_STATE)//���Կհ�
				{
					ss<<ch;// �洢�������ַ�
				}
				break;
			}
			case UNSIGNED_INTEGER_STATE:// ��ǰ״̬���޷�������
			{
// ����գ�
// �����ǰ�Ѿ��������ļ�β��������Ѿ��������ַ���Ϊ����
//     �����ɹ��򷵻��޷����������͵�token�����򷵻ر������
				if(!current_char.has_value())
				{
					std::string temp1;
					ss>>temp1;
					ss.clear();
					int ans;
					if(temp1=="2147483648")
					{
						ans=-2147483648;
						return std::make_pair(std::make_optional<Token>(TokenType::UNSIGNED_INTEGER,ans,pos,currentPos()),std::optional<CompilationError>());
					}
					try
					{
						ans=std::stoi(temp1);
						return std::make_pair(std::make_optional<Token>(TokenType::UNSIGNED_INTEGER,ans,pos,currentPos()),std::optional<CompilationError>());
					}
					catch(std::exception e)
					{
						return std::make_pair(std::optional<Token>(),std::make_optional<CompilationError>(0,0,ErrIntegerOverflow));
					}
				}
// ����������ַ������֣���洢�������ַ�
				auto ch=current_char.value();
				if(miniplc0::isdigit(ch))
				{
					std::string temp2(1,ch);
					ss<<temp2;
				}
// ����������ַ��������֣�����˶������ַ����������Ѿ��������ַ���Ϊ����
//     �����ɹ��򷵻��޷����������͵�token�����򷵻ر������
				else
				{
					unreadLast();
					std::string temp1;
					ss>>temp1;
					ss.clear();
					int ans;
					if(temp1=="2147483648")
					{
						ans=-2147483648;
						return std::make_pair(std::make_optional<Token>(TokenType::UNSIGNED_INTEGER,ans,pos,currentPos()),std::optional<CompilationError>());
					}
					try
					{
						ans=std::stoi(temp1);
						return std::make_pair(std::make_optional<Token>(TokenType::UNSIGNED_INTEGER,ans,pos,currentPos()),std::optional<CompilationError>());
					}
					catch(std::exception e)
					{
						return std::make_pair(std::optional<Token>(),std::make_optional<CompilationError>(0,0,ErrIntegerOverflow));
					}
				}
				break;
			}
			case IDENTIFIER_STATE:
			{
// ����գ�
// �����ǰ�Ѿ��������ļ�β��������Ѿ��������ַ���
//     �����������ǹؼ��֣���ô���ض�Ӧ�ؼ��ֵ�token�����򷵻ر�ʶ����token
				if(!current_char.has_value())
				{
					std::string temp1;
					ss>>temp1;
					ss.clear();
					if(temp1=="begin")
					{
						return std::make_pair(std::make_optional<Token>(TokenType::BEGIN,temp1,pos,currentPos()),std::optional<CompilationError>());
					}
					else if(temp1=="end")
					{
						return std::make_pair(std::make_optional<Token>(TokenType::END,temp1,pos,currentPos()),std::optional<CompilationError>());
					}
					else if(temp1=="const")
					{
						return std::make_pair(std::make_optional<Token>(TokenType::CONST,temp1,pos,currentPos()),std::optional<CompilationError>());
					}
					else if(temp1=="var")
					{
						return std::make_pair(std::make_optional<Token>(TokenType::VAR,temp1,pos,currentPos()),std::optional<CompilationError>());
					}
					else if(temp1=="print")
					{
						return std::make_pair(std::make_optional<Token>(TokenType::PRINT,temp1,pos,currentPos()),std::optional<CompilationError>());
					}
					else
					{
						return std::make_pair(std::make_optional<Token>(TokenType::IDENTIFIER,temp1,pos,currentPos()),std::optional<CompilationError>());
					}
				}
// ������������ַ�����ĸ����洢�������ַ�
				auto ch=current_char.value();
				if(miniplc0::isdigit(ch)||miniplc0::isalpha(ch))
				{
					std::string temp2(1,ch);
					ss<<temp2;
				}
// ����������ַ������������֮һ������˶������ַ����������Ѿ��������ַ���
//     �����������ǹؼ��֣���ô���ض�Ӧ�ؼ��ֵ�token�����򷵻ر�ʶ����token
				else
				{
					unreadLast();
					std::string temp1;
					ss>>temp1;
					ss.clear();
					if(temp1=="begin")
					{
						return std::make_pair(std::make_optional<Token>(TokenType::BEGIN,temp1,pos,currentPos()),std::optional<CompilationError>());
					}
					else if(temp1=="end")
					{
						return std::make_pair(std::make_optional<Token>(TokenType::END,temp1,pos,currentPos()),std::optional<CompilationError>());
					}
					else if(temp1=="const")
					{
						return std::make_pair(std::make_optional<Token>(TokenType::CONST,temp1,pos,currentPos()),std::optional<CompilationError>());
					}
					else if(temp1=="var")
					{
						return std::make_pair(std::make_optional<Token>(TokenType::VAR,temp1,pos,currentPos()),std::optional<CompilationError>());
					}
					else if(temp1=="print")
					{
						return std::make_pair(std::make_optional<Token>(TokenType::PRINT,temp1,pos,currentPos()),std::optional<CompilationError>());
					}
					else
					{
						return std::make_pair(std::make_optional<Token>(TokenType::IDENTIFIER,temp1,pos,currentPos()),std::optional<CompilationError>());
					}
				}
				break;
			}
			case PLUS_SIGN_STATE:// �����ǰ״̬�ǼӺ�
			{
// ��˼������ΪʲôҪ���ˣ��������ط��᲻����Ҫ
				unreadLast();//�ǵģ�����û�ж����һ���ַ�����ʹ����һ��EOF��
				return std::make_pair(std::make_optional<Token>(TokenType::PLUS_SIGN,'+',pos,currentPos()),std::optional<CompilationError>());
			}
			case MINUS_SIGN_STATE:// ��ǰ״̬Ϊ���ŵ�״̬
			{
// ����գ����ˣ������ؼ���token
				unreadLast();
				return std::make_pair(std::make_optional<Token>(TokenType::MINUS_SIGN,'-',pos,currentPos()),std::optional<CompilationError>());
			}
// ����գ�
// ���������ĺϷ�״̬�����к��ʵĲ���
// ������н���������token�����ر������
			case MULTIPLICATION_SIGN_STATE:
			{
				unreadLast();
				return std::make_pair(std::make_optional<Token>(TokenType::MULTIPLICATION_SIGN,'*',pos,currentPos()),std::optional<CompilationError>());
			}
			case DIVISION_SIGN_STATE:
			{
				unreadLast();
				return std::make_pair(std::make_optional<Token>(TokenType::DIVISION_SIGN,'/',pos,currentPos()),std::optional<CompilationError>());
			}
			case EQUAL_SIGN_STATE:
			{
				unreadLast();
				return std::make_pair(std::make_optional<Token>(TokenType::EQUAL_SIGN,'=',pos,currentPos()),std::optional<CompilationError>());
			}
			case SEMICOLON_STATE:
			{
				unreadLast();
				return std::make_pair(std::make_optional<Token>(TokenType::SEMICOLON,';',pos,currentPos()),std::optional<CompilationError>());
			}
			case LEFTBRACKET_STATE:
			{
				unreadLast();
				return std::make_pair(std::make_optional<Token>(TokenType::LEFT_BRACKET,'(',pos,currentPos()),std::optional<CompilationError>());
			}
			case RIGHTBRACKET_STATE:
			{
				unreadLast();
				return std::make_pair(std::make_optional<Token>(TokenType::RIGHT_BRACKET,')',pos,currentPos()),std::optional<CompilationError>());
			}
// Ԥ��֮���״̬�����ִ�е������˵�������쳣
			default:
				DieAndPrint("unhandled state.");
			break;
		}
	}
// Ԥ��֮���״̬�����ִ�е������˵�������쳣
	return std::make_pair(std::optional<Token>(),std::optional<CompilationError>());
}

std::optional<CompilationError> Tokenizer::checkToken(const Token& t)// ��� Token �ĺϷ���
{
	switch(t.GetType())
	{
		case IDENTIFIER:
		{
			auto val=t.GetValueString();
			if(miniplc0::isdigit(val[0]))
			{
				return std::make_optional<CompilationError>(t.GetStartPos().first, t.GetStartPos().second,ErrorCode::ErrInvalidIdentifier);
			}
			break;
		}
		default:
		break;
	}
	return {};
}

void Tokenizer::readAll()
{
	if(_initialized)
	{
		return;
	}
	for(std::string tp; std::getline(_rdr, tp);)
	{
		_lines_buffer.emplace_back(std::move(tp + "\n"));
	}
	_initialized = true;
	_ptr = std::make_pair<int64_t, int64_t>(0, 0);
	return;
}

//ע�⣺����std::vector::end()����ƣ���������˺�������һ�������߽��position��
std::pair<uint64_t, uint64_t> Tokenizer::nextPos()
{
	if(_ptr.first >= _lines_buffer.size())
	{
		DieAndPrint("advance after EOF");
	}
	if (_ptr.second == _lines_buffer[_ptr.first].size() - 1)
	{
		return std::make_pair(_ptr.first + 1, 0);
	}
	else
	{
		return std::make_pair(_ptr.first, _ptr.second + 1);
	}
}

std::pair<uint64_t, uint64_t> Tokenizer::currentPos()
{
	return _ptr;
}

std::pair<uint64_t, uint64_t> Tokenizer::previousPos()
{
	if (_ptr.first == 0 && _ptr.second == 0)
	{
		DieAndPrint("previous position from beginning");
	}
	if (_ptr.second == 0)
	{
		return std::make_pair(_ptr.first - 1,_lines_buffer[_ptr.first - 1].size() - 1);
	}
	else
	{
		return std::make_pair(_ptr.first, _ptr.second - 1);
	}
}

std::optional<char> Tokenizer::nextChar()
{
	if(isEOF())
	{
		return {};
	}
	auto result=_lines_buffer[_ptr.first][_ptr.second];
	_ptr=nextPos();
	return result;
}

bool Tokenizer::isEOF()
{
	return _ptr.first>=_lines_buffer.size();
}

//ע�⣺δ���������Ƿ��к���
void Tokenizer::unreadLast()
{
	_ptr=previousPos();
}

}  // namespace miniplc0
