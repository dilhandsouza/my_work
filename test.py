import datetime

def calculate_age(birthdate):
    today = datetime.date.today()
    
    # Extract year, month, day from the date of birth
    birth_year = int(birthdate[:4])
    birth_month = int(birthdate[5:7])
    birth_day = int(birthdate[-2:])
    
    current_year = today.year
    current_month = today.month
    current_day = today.day
    
    age = current_year - birth_year
    if (birth_month > current_month) or \
       (birth_month == current_month and birth_day > current_day):
        age -= 1
        
    return age

# Example usage:
user_dob = input("Please enter your date of birth in the format YYYY-MM-DD: ")
try:
    user_age = calculate_age(user_dob)
    print(f"You are {user_age} years old.")
except ValueError as e:
    print(str(e))