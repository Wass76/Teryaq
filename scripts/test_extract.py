#!/usr/bin/env python3
"""
Test script for extract_for_spring_boot.py
This script tests the pharmaceutical data extraction functionality
"""

import subprocess
import sys
import os
import json

def test_extract_script():
    """Test the extract_for_spring_boot.py script"""
    
    # Get the directory of this script
    script_dir = os.path.dirname(os.path.abspath(__file__))
    extract_script = os.path.join(script_dir, "extract_for_spring_boot.py")
    
    # Test file path (you can change this to your actual test file)
    test_file = os.path.join(os.path.dirname(script_dir), "testp.xlsx")
    
    # Database config (using SQLite for testing)
    db_config = "sqlite:///test_pharmaceutical.db"
    
    print(f"Testing script: {extract_script}")
    print(f"Test file: {test_file}")
    print(f"Database config: {db_config}")
    
    # Check if files exist
    if not os.path.exists(extract_script):
        print(f"ERROR: Extract script not found: {extract_script}")
        return False
        
    if not os.path.exists(test_file):
        print(f"ERROR: Test file not found: {test_file}")
        return False
    
    try:
        # Run the script
        result = subprocess.run([
            sys.executable, extract_script, test_file, db_config
        ], capture_output=True, text=True, timeout=60)
        
        print(f"Exit code: {result.returncode}")
        print(f"STDOUT: {result.stdout}")
        print(f"STDERR: {result.stderr}")
        
        if result.returncode == 0:
            try:
                # Try to parse the JSON output
                data = json.loads(result.stdout)
                print(f"Successfully parsed {len(data)} records")
                
                # Print first record as example
                if data:
                    print("First record:")
                    print(json.dumps(data[0], indent=2, ensure_ascii=False))
                
                return True
            except json.JSONDecodeError as e:
                print(f"ERROR: Failed to parse JSON output: {e}")
                return False
        else:
            print("ERROR: Script failed")
            return False
            
    except subprocess.TimeoutExpired:
        print("ERROR: Script timed out")
        return False
    except Exception as e:
        print(f"ERROR: {e}")
        return False

if __name__ == "__main__":
    success = test_extract_script()
    sys.exit(0 if success else 1)
